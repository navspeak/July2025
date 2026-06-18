package com.example.encryption.cli;

import com.example.encryption.domain.EncryptionAlgorithm;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.nio.file.Path;

@Component
@Profile("cli")
@Command(name = "encrypt-file", mixinStandardHelpOptions = true, description = "Encrypt a single file")
public class EncryptFileCommand implements Runnable {

    @Option(names = "--file", required = true, description = "File to encrypt")
    private Path file;

    @Option(names = "--out", required = true, description = "Output .enc file path")
    private Path out;

    @Option(names = "--algorithm", description = "AES_256_GCM (default) or CHACHA20_POLY1305")
    private EncryptionAlgorithm algorithm;

    @Option(names = "--transit-key", description = "Vault transit key name")
    private String transitKey;

    private final PathEncryptionService encryptionService;

    public EncryptFileCommand(PathEncryptionService encryptionService) {
        this.encryptionService = encryptionService;
    }

    @Override
    public void run() {
        try {
            encryptionService.encryptFile(file, out, algorithm, transitKey);
            System.out.printf("encrypted: %s -> %s%n", file, out);
        } catch (Exception e) {
            throw new RuntimeException("encrypt-file failed: " + e.getMessage(), e);
        }
    }
}
