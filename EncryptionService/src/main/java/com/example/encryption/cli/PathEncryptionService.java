package com.example.encryption.cli;

import com.example.encryption.domain.EncryptionAlgorithm;
import com.example.encryption.domain.FileEncryptionContext;
import com.example.encryption.domain.FileEncryptionMetadata;
import com.example.encryption.processor.EncryptionProcessor;
import com.example.encryption.util.FileNameUtils;
import tools.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.List;

@Slf4j
@Service
@Profile("cli")
public class PathEncryptionService {

    private final EncryptionProcessor encryptionProcessor;
    private final EncryptionClient encryptionClient;
    private final ObjectMapper objectMapper;

    public PathEncryptionService(EncryptionProcessor encryptionProcessor,
                                  EncryptionClient encryptionClient,
                                  ObjectMapper objectMapper) {
        this.encryptionProcessor = encryptionProcessor;
        this.encryptionClient = encryptionClient;
        this.objectMapper = objectMapper;
    }

    public record FileEncryptTask(Path input, Path output) {}

    // Single file — fresh DEK, one remote key-wrap call
    public void encryptFile(Path input, Path output,
                            EncryptionAlgorithm algorithm, String transitKey) throws Exception {
        EncryptionAlgorithm alg = algorithm != null ? algorithm : EncryptionAlgorithm.AES_256_GCM;
        FileEncryptionContext ctx = encryptionProcessor.initEncrypt(alg);
        String wrappedDek = encryptionClient.encryptText(ctx.dekBase64(), transitKey);
        writeEncrypted(input, output, ctx, wrappedDek, alg);
    }

    // Bulk — shared DEK, ONE remote key-wrap call for the entire batch
    public void encryptFiles(List<FileEncryptTask> tasks,
                             EncryptionAlgorithm algorithm, String transitKey) throws Exception {
        if (tasks.isEmpty()) return;
        EncryptionAlgorithm alg = algorithm != null ? algorithm : EncryptionAlgorithm.AES_256_GCM;

        byte[] keyBytes = encryptionProcessor.generateKey(alg);
        String dekBase64 = Base64.getEncoder().encodeToString(keyBytes);
        String wrappedDek = encryptionClient.encryptText(dekBase64, transitKey);
        log.debug("Bulk encrypt: {} files, one shared DEK", tasks.size());

        for (FileEncryptTask task : tasks) {
            FileEncryptionContext ctx = encryptionProcessor.initEncryptWithKey(keyBytes, alg);
            writeEncrypted(task.input(), task.output(), ctx, wrappedDek, alg);
        }
    }

    public void decryptFile(Path input, Path output, String transitKey) throws Exception {
        try (InputStream in = safeOpen(input)) {
            int metadataLen = ByteBuffer.wrap(in.readNBytes(4)).getInt();
            FileEncryptionMetadata metadata = objectMapper.readValue(in.readNBytes(metadataLen), FileEncryptionMetadata.class);
            String dekBase64 = encryptionClient.decryptText(metadata.wrappedDek(), transitKey);
            byte[] decrypted = encryptionProcessor.initDecryptCipher(metadata, dekBase64).doFinal(in.readAllBytes());
            Files.write(output, decrypted);
        }
    }

    private void writeEncrypted(Path input, Path output,
                                 FileEncryptionContext ctx, String wrappedDek,
                                 EncryptionAlgorithm algorithm) throws Exception {
        FileEncryptionMetadata metadata = new FileEncryptionMetadata(
                FileNameUtils.sanitize(input.getFileName().toString()),
                Base64.getEncoder().encodeToString(ctx.iv()),
                wrappedDek,
                algorithm);

        byte[] metadataBytes = objectMapper.writeValueAsBytes(metadata);
        byte[] encrypted = ctx.cipher().doFinal(safeRead(input));

        try (OutputStream out = Files.newOutputStream(output)) {
            out.write(ByteBuffer.allocate(4).putInt(metadataBytes.length).array());
            out.write(metadataBytes);
            out.write(encrypted);
        }
    }

    private byte[] safeRead(Path path) throws IOException {
        Path safe = path.toAbsolutePath().normalize();
        if (!Files.isRegularFile(safe)) {
            throw new IllegalArgumentException("Not a regular file: " + safe);
        }
        return Files.readAllBytes(safe);
    }

    private InputStream safeOpen(Path path) throws IOException {
        Path safe = path.toAbsolutePath().normalize();
        if (!Files.isRegularFile(safe)) {
            throw new IllegalArgumentException("Not a regular file: " + safe);
        }
        return Files.newInputStream(safe);
    }
}
