package com.example.encryption.service;

import com.example.encryption.domain.EncryptionAlgorithm;
import com.example.encryption.domain.FileEncryptionContext;
import com.example.encryption.domain.FileEncryptionMetadata;
import com.example.encryption.domain.Operation;
import com.example.encryption.domain.StagingPath;
import com.example.encryption.dto.EncryptionRequest;
import com.example.encryption.processor.EncryptionProcessor;
import com.example.encryption.processor.FileProcessor;
import com.example.encryption.vault.VaultTransitService;
import com.example.encryption.util.FileNameUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.resilience4j.bulkhead.Bulkhead;
import io.github.resilience4j.bulkhead.BulkheadRegistry;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.util.Base64;
import java.util.Optional;
// import java.util.concurrent.Semaphore;

@Service
@Profile("!client")
public class FileEncryptionService {

    private static final long MAX_FILE_BYTES = 1024 * 1024; // 1 MB — beyond this doFinal() risks OOM under concurrency

    // Resilience4j semaphore bulkhead — config in application.yml under resilience4j.bulkhead.instances.cipherOps
    // Limits concurrent doFinal() calls so heap usage stays bounded under load (~2 MB per op × 10 = ~20 MB peak).
    // Publishes metrics to Micrometer automatically (resilience4j.bulkhead.* tags).
    private final Bulkhead cipherBulkhead;

    // Alternative: plain Java semaphore — no metrics, no config, but zero dependencies.
    // private static final int MAX_CONCURRENT_CIPHER_OPS = 10;
    // private final Semaphore cipherBulkhead = new Semaphore(MAX_CONCURRENT_CIPHER_OPS);

    private final EncryptionProcessor encryptionProcessor;
    private final FileProcessor fileProcessor;
    private final VaultTransitService vaultTransitService;
    private final ObjectMapper objectMapper;
    private final MeterRegistry meterRegistry;

    public FileEncryptionService(EncryptionProcessor encryptionProcessor,
                                 FileProcessor fileProcessor,
                                 VaultTransitService vaultTransitService,
                                 ObjectMapper objectMapper,
                                 MeterRegistry meterRegistry,
                                 BulkheadRegistry bulkheadRegistry) {
        this.encryptionProcessor = encryptionProcessor;
        this.fileProcessor = fileProcessor;
        this.vaultTransitService = vaultTransitService;
        this.objectMapper = objectMapper;
        this.meterRegistry = meterRegistry;
        this.cipherBulkhead = bulkheadRegistry.bulkhead("cipherOps");
    }

    public StreamingResponseBody encryptFile(MultipartFile file, EncryptionRequest request) throws Exception {
        Optional<EncryptionRequest> req = Optional.ofNullable(request);
        String transitKey = req.map(EncryptionRequest::transitKey).orElse(null);
        EncryptionAlgorithm algorithm = req.map(EncryptionRequest::algorithm).orElse(EncryptionAlgorithm.AES_256_GCM);

        StagingPath paths = stage(file, Operation.ENCRYPT);
        try {
            writeEncrypted(file, paths, algorithm, transitKey);
            return streamOutput(paths, "encrypt");
        } catch (Exception e) {
            fileProcessor.cleanup(paths);
            throw e;
        }
    }

    public StreamingResponseBody decryptFile(MultipartFile file, String transitKey) throws Exception {
        StagingPath paths = stage(file, Operation.DECRYPT);
        try {
            writeDecrypted(paths, transitKey);
            return streamOutput(paths, "decrypt");
        } catch (Exception e) {
            fileProcessor.cleanup(paths);
            throw e;
        }
    }

    private StagingPath stage(MultipartFile file, Operation op) throws Exception {
        StagingPath paths = fileProcessor.resolvePaths(file, op);
        Timer.Sample sample = Timer.start(meterRegistry);
        fileProcessor.stageInput(file, paths.inputPath());
        sample.stop(timer("file.stage.latency", op.name().toLowerCase()));
        return paths;
    }

    private void writeEncrypted(MultipartFile file, StagingPath paths,
                                EncryptionAlgorithm algorithm, String transitKey) throws Exception {
        FileEncryptionContext ctx = encryptionProcessor.initEncrypt(algorithm);
        String wrappedDek = vaultTransitService.wrapDek(ctx.dekBase64(), transitKey);

        FileEncryptionMetadata metadata = new FileEncryptionMetadata(
                FileNameUtils.sanitize(file.getOriginalFilename()),
                Base64.getEncoder().encodeToString(ctx.iv()),
                wrappedDek, algorithm);
        byte[] metadataBytes = objectMapper.writeValueAsBytes(metadata);

        // Output file layout (paths.outputPath):
        //   bytes 0-3           : metadata length (4-byte big-endian int)
        //   bytes 4-(4+N-1)     : metadata JSON (N bytes)
        //   bytes (4+N) onwards : AES-GCM / ChaCha20 encrypted file content
        // doFinal() instead of CipherInputStream: AES-GCM and ChaCha20-Poly1305 both append an auth tag
        // at the end of the ciphertext. JCE will not release any plaintext until the tag is verified,
        // so CipherInputStream buffers the entire input internally anyway — with worse latency and
        // no reduction in memory usage. doFinal() is explicit about the full-buffer contract.
        //
        // To support files larger than MAX_FILE_BYTES, switch to a chunked scheme: split the file into
        // fixed-size chunks (e.g. 64KB), encrypt each chunk independently with its own IV and auth tag,
        // and write chunk boundaries into the metadata. Decryption then verifies each chunk's tag before
        // releasing that chunk's plaintext — true streaming with per-chunk integrity.
        assertSafeForDoFinal(paths.inputPath());
        cipherBulkhead.acquirePermission(); // throws BulkheadFullException if maxConcurrentCalls exceeded
        // cipherBulkhead.tryAcquire();     // semaphore equivalent
        Timer.Sample sample = Timer.start(meterRegistry);
        try {
            byte[] encrypted = ctx.cipher().doFinal(Files.readAllBytes(paths.inputPath()));
            sample.stop(timer("file.cipher.latency", "encrypt"));
            try (OutputStream out = Files.newOutputStream(paths.outputPath())) {
                out.write(ByteBuffer.allocate(4).putInt(metadataBytes.length).array());
                out.write(metadataBytes);
                out.write(encrypted);
            }
        } finally {
            cipherBulkhead.releasePermission();
            // cipherBulkhead.release();    // semaphore equivalent
        }
    }

    private void writeDecrypted(StagingPath paths, String transitKey) throws Exception {
        assertSafeForDoFinal(paths.inputPath());
        cipherBulkhead.acquirePermission(); // throws BulkheadFullException if maxConcurrentCalls exceeded
        // cipherBulkhead.tryAcquire();     // semaphore equivalent
        try (InputStream in = Files.newInputStream(paths.inputPath())) {
            FileEncryptionMetadata metadata = readMetadata(in);
            String dekBase64 = vaultTransitService.unwrapDek(metadata.wrappedDek(), transitKey);
            // Same reasoning as encrypt: GCM/Poly1305 auth tag forces JCE to buffer everything
            // before releasing plaintext — CipherInputStream gives no streaming benefit here.
            Timer.Sample sample = Timer.start(meterRegistry);
            byte[] decrypted = encryptionProcessor.initDecryptCipher(metadata, dekBase64)
                    .doFinal(in.readAllBytes());
            sample.stop(timer("file.cipher.latency", "decrypt"));
            Files.write(paths.outputPath(), decrypted);
        } finally {
            cipherBulkhead.releasePermission();
            // cipherBulkhead.release();    // semaphore equivalent
        }
    }

    private void assertSafeForDoFinal(java.nio.file.Path path) throws Exception {
        long size = Files.size(path);
        if (size > MAX_FILE_BYTES) {
            throw new IllegalArgumentException(
                    "File size " + size + " bytes exceeds doFinal() safe limit of " + MAX_FILE_BYTES + " bytes");
        }
    }

    private FileEncryptionMetadata readMetadata(InputStream in) throws Exception {
        int metadataLen = ByteBuffer.wrap(in.readNBytes(4)).getInt();
        return objectMapper.readValue(in.readNBytes(metadataLen), FileEncryptionMetadata.class);
    }

    private StreamingResponseBody streamOutput(StagingPath paths, String operation) {
        return outputStream -> {
            Timer.Sample sample = Timer.start(meterRegistry);
            try (InputStream in = Files.newInputStream(paths.outputPath())) {
                in.transferTo(outputStream);
            } finally {
                sample.stop(timer("file.stream.latency", operation));
                fileProcessor.cleanup(paths);
            }
        };
    }

    private Timer timer(String name, String operation) {
        return Timer.builder(name)
                .tag("operation", operation)
                .register(meterRegistry);
    }
}
