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
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.util.Base64;

@Service
public class FileEncryptionService {

    private final EncryptionProcessor encryptionProcessor;
    private final FileProcessor fileProcessor;
    private final VaultTransitService vaultTransitService;
    private final ObjectMapper objectMapper;

    public FileEncryptionService(EncryptionProcessor encryptionProcessor,
                                 FileProcessor fileProcessor,
                                 VaultTransitService vaultTransitService,
                                 ObjectMapper objectMapper) {
        this.encryptionProcessor = encryptionProcessor;
        this.fileProcessor = fileProcessor;
        this.vaultTransitService = vaultTransitService;
        this.objectMapper = objectMapper;
    }

    public StreamingResponseBody encryptFile(MultipartFile file, EncryptionRequest request) throws Exception {
        String transitKey = request != null ? request.transitKey() : null;
        String fileName = (request != null && request.fileName() != null)
                ? request.fileName() : file.getOriginalFilename();
        EncryptionAlgorithm algorithm = request != null
                ? request.algorithm() : EncryptionAlgorithm.AES_256_GCM;

        StagingPath paths = fileProcessor.getPaths(file, Operation.ENCRYPT);
        try {
            FileEncryptionContext ctx = encryptionProcessor.initEncrypt(algorithm);
            String wrappedDek = vaultTransitService.wrapDek(ctx.dekBase64(), transitKey);

            String ivBase64 = Base64.getEncoder().encodeToString(ctx.iv());
            FileEncryptionMetadata metadata = new FileEncryptionMetadata(fileName, ivBase64, wrappedDek, algorithm);
            byte[] metadataBytes = objectMapper.writeValueAsBytes(metadata);

            // Output format: [4-byte metadata length][metadata JSON][AES-GCM encrypted file]
            try (InputStream in = Files.newInputStream(paths.inputPath());
                 OutputStream out = Files.newOutputStream(paths.outputPath())) {

                out.write(ByteBuffer.allocate(4).putInt(metadataBytes.length).array());
                out.write(metadataBytes);

                try (CipherOutputStream cos = new CipherOutputStream(out, ctx.cipher())) {
                    in.transferTo(cos);
                }
            }

            return outputStream -> {
                try (InputStream in = Files.newInputStream(paths.outputPath())) {
                    in.transferTo(outputStream);
                } finally {
                    fileProcessor.cleanup(paths);
                }
            };
        } catch (Exception e) {
            fileProcessor.cleanup(paths);
            throw e;
        }
    }

    public StreamingResponseBody decryptFile(MultipartFile file, String transitKey) throws Exception {
        StagingPath paths = fileProcessor.getPaths(file, Operation.DECRYPT);
        try {
            try (InputStream in = Files.newInputStream(paths.inputPath())) {
                int metadataLen = ByteBuffer.wrap(in.readNBytes(4)).getInt();
                FileEncryptionMetadata metadata = objectMapper.readValue(in.readNBytes(metadataLen),
                        FileEncryptionMetadata.class);

                String dekBase64 = vaultTransitService.unwrapDek(metadata.wrappedDek(), transitKey);

                try (CipherInputStream cis = new CipherInputStream(in,
                        encryptionProcessor.initDecryptCipher(metadata, dekBase64));
                     OutputStream out = Files.newOutputStream(paths.outputPath())) {
                    cis.transferTo(out);
                }
            }

            return outputStream -> {
                try (InputStream in = Files.newInputStream(paths.outputPath())) {
                    in.transferTo(outputStream);
                } finally {
                    fileProcessor.cleanup(paths);
                }
            };
        } catch (Exception e) {
            fileProcessor.cleanup(paths);
            throw e;
        }
    }
}
