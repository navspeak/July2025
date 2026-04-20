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

@Service
@Profile("!client")
public class FileEncryptionService {

    private final EncryptionProcessor encryptionProcessor;
    private final FileProcessor fileProcessor;
    private final VaultTransitService vaultTransitService;
    private final ObjectMapper objectMapper;
    private final MeterRegistry meterRegistry;

    public FileEncryptionService(EncryptionProcessor encryptionProcessor,
                                 FileProcessor fileProcessor,
                                 VaultTransitService vaultTransitService,
                                 ObjectMapper objectMapper,
                                 MeterRegistry meterRegistry) {
        this.encryptionProcessor = encryptionProcessor;
        this.fileProcessor = fileProcessor;
        this.vaultTransitService = vaultTransitService;
        this.objectMapper = objectMapper;
        this.meterRegistry = meterRegistry;
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
        Timer.Sample sample = Timer.start(meterRegistry);
        byte[] encrypted = ctx.cipher().doFinal(Files.readAllBytes(paths.inputPath()));
        sample.stop(timer("file.cipher.latency", "encrypt"));
        try (OutputStream out = Files.newOutputStream(paths.outputPath())) {
            out.write(ByteBuffer.allocate(4).putInt(metadataBytes.length).array());
            out.write(metadataBytes);
            out.write(encrypted);
        }
    }

    private void writeDecrypted(StagingPath paths, String transitKey) throws Exception {
        try (InputStream in = Files.newInputStream(paths.inputPath())) {
            FileEncryptionMetadata metadata = readMetadata(in);
            String dekBase64 = vaultTransitService.unwrapDek(metadata.wrappedDek(), transitKey);
            Timer.Sample sample = Timer.start(meterRegistry);
            byte[] decrypted = encryptionProcessor.initDecryptCipher(metadata, dekBase64)
                    .doFinal(in.readAllBytes());
            sample.stop(timer("file.cipher.latency", "decrypt"));
            Files.write(paths.outputPath(), decrypted);
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
