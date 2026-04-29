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

        try {
            long t0 = System.nanoTime();
            EncryptionResult enc = client.encrypt(inputFile, "my-key", "AES_256_GCM");
            long encMs = elapsed(t0);

            long decMs = 0;
            EncryptionResult dec = null;
            if (enc.isSuccess() && enc.path() != null) {
                long t1 = System.nanoTime();
                dec = client.decrypt(enc.path(), "my-key");
                decMs = elapsed(t1);
            }

            long roundMs = elapsed(t0);

            System.out.printf("%n=== Round-trip ping (file=%d KB) ===%n", FILE_SIZE_BYTES / 1024);
            System.out.printf("  encrypt=%d ms  decrypt=%d ms  total=%d ms%n", encMs, decMs, roundMs);
            System.out.printf("  traceId=%s  encStatus=%d  decStatus=%s%n",
                    enc.traceId(),
                    enc.status(),
                    dec != null ? dec.status() : "-");
            System.out.println("  Server log: grep \"ROUND_TRIP\" app.log | tail -2");
        } finally {
            Files.deleteIfExists(inputFile);
        }
    }

    private long elapsed(long startNano) {
        return (System.nanoTime() - startNano) / 1_000_000;
    }
}
