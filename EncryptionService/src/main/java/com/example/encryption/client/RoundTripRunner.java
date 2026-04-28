package com.example.encryption.client;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Random;

/**
 * Run with: --spring.profiles.active=ping
 *
 * Performs a single encrypt → decrypt cycle and prints client-side timing.
 * Correlate with server logs: grep "ROUND_TRIP" app.log | tail -2
 */
@Component
@Profile("ping")
public class RoundTripRunner implements CommandLineRunner {

    private static final int FILE_SIZE_BYTES = 10 * 1024; // 10 KB

    private final FileEncryptionClient client;

    public RoundTripRunner(FileEncryptionClient client) {
        this.client = client;
    }

    @Override
    public void run(String... args) throws Exception {
        byte[] data = new byte[FILE_SIZE_BYTES];
        new Random().nextBytes(data);

        Path inputFile = Path.of("ping_test.bin");
        Files.write(inputFile, data);

        Path encFile = null;
        Path decFile = null;
        try {
            long t0 = System.nanoTime();
            encFile = client.encrypt(inputFile, "my-key", "AES_256_GCM");
            long encMs = elapsed(t0);

            long t1 = System.nanoTime();
            decFile = client.decrypt(encFile, "my-key");
            long decMs = elapsed(t1);

            long roundMs = elapsed(t0);

            System.out.printf("%n=== Round-trip ping (file=%d KB) ===%n", FILE_SIZE_BYTES / 1024);
            System.out.printf("  encrypt=%d ms  decrypt=%d ms  total=%d ms%n", encMs, decMs, roundMs);
            System.out.println("  Server log: grep \"ROUND_TRIP\" app.log | tail -2");
        } finally {
            Files.deleteIfExists(inputFile);
            if (encFile != null) Files.deleteIfExists(encFile);
            if (decFile != null) Files.deleteIfExists(decFile);
        }
    }

    private long elapsed(long startNano) {
        return (System.nanoTime() - startNano) / 1_000_000;
    }
}
