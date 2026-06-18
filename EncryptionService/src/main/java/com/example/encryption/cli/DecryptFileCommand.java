package com.example.encryption.cli;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.nio.file.Path;

@Component
@Profile("cli")
@Command(name = "decrypt-file", mixinStandardHelpOptions = true, description = "Decrypt a single .enc file")
public class DecryptFileCommand implements Runnable {

    @Option(names = "--file", required = true, description = "Encrypted .enc file")
    private Path file;

    @Option(names = "--out", required = true, description = "Output file path")
    private Path out;

    @Option(names = "--transit-key", description = "Vault transit key name")
    private String transitKey;

    private final PathEncryptionService encryptionService;

    public DecryptFileCommand(PathEncryptionService encryptionService) {
        this.encryptionService = encryptionService;
    }

    @Override
    public void run() {
        try {
            encryptionService.decryptFile(file, out, transitKey);
            System.out.printf("decrypted: %s -> %s%n", file, out);
        } catch (Exception e) {
            throw new RuntimeException("decrypt-file failed: " + e.getMessage(), e);
        }
    }
}
