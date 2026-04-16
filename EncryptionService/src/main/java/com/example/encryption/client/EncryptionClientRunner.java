package com.example.encryption.client;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.nio.file.Path;

/**
 * Run with: --spring.profiles.active=client
 * Place a test file at /tmp/sample.csv before running.
 */
@Component
@Profile("client")
public class EncryptionClientRunner implements CommandLineRunner {

    private final EncryptionServiceClient client;

    public EncryptionClientRunner(EncryptionServiceClient client) {
        this.client = client;
    }

    @Override
    public void run(String... args) throws Exception {
        Path inputFile = Path.of("/tmp/sample.csv");

        // --- Encrypt with AES-256-GCM (default) ---
        System.out.println("Encrypting: " + inputFile);
        Path encryptedFile = client.encrypt(inputFile, "my-key", "AES_256_GCM");
        System.out.println("Encrypted file: " + encryptedFile);

        // --- Decrypt ---
        System.out.println("Decrypting: " + encryptedFile);
        Path decryptedFile = client.decrypt(encryptedFile, "my-key");
        System.out.println("Decrypted file: " + decryptedFile);

        // --- Encrypt with ChaCha20 ---
        System.out.println("Encrypting with ChaCha20-Poly1305: " + inputFile);
        Path chachaEncrypted = client.encrypt(inputFile, "my-key", "CHACHA20_POLY1305");
        System.out.println("ChaCha20 encrypted file: " + chachaEncrypted);
    }
}
