package com.example.encryption.client;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Run with: --spring.profiles.active=client
 */
@Component
@Profile("client")
public class EncryptionClientRunner implements CommandLineRunner {

    private static final int ITERATIONS = 100;
    private static final int FILE_SIZE_BYTES = 1024 * 1024; // 1 MB

    private final FileEncryptionClient client;

    public EncryptionClientRunner(FileEncryptionClient client) {
        this.client = client;
    }

    @Override
    public void run(String... args) throws Exception {
        Path inputFile = Path.of("benchmark_input.bin");

        System.out.println("Generating 1 MB random file: " + inputFile.toAbsolutePath());
        byte[] data = new byte[FILE_SIZE_BYTES];
        new Random().nextBytes(data);
        Files.write(inputFile, data);

        List<Long> encryptLatencies = new ArrayList<>(ITERATIONS);
        List<Long> decryptLatencies = new ArrayList<>(ITERATIONS);
        List<Long> roundTripLatencies = new ArrayList<>(ITERATIONS);

        System.out.printf("Running %d encrypt/decrypt iterations...%n", ITERATIONS);

        for (int i = 1; i <= ITERATIONS; i++) {
            long roundStart = System.nanoTime();

            long t0 = System.nanoTime();
            Path encryptedFile = client.encrypt(inputFile, "my-key", "AES_256_GCM");
            long encryptMs = (System.nanoTime() - t0) / 1_000_000;
            encryptLatencies.add(encryptMs);

            long t1 = System.nanoTime();
            Path decryptedFile = client.decrypt(encryptedFile, "my-key");
            long decryptMs = (System.nanoTime() - t1) / 1_000_000;
            decryptLatencies.add(decryptMs);

            long roundTripMs = (System.nanoTime() - roundStart) / 1_000_000;
            roundTripLatencies.add(roundTripMs);

            Files.deleteIfExists(encryptedFile);
            Files.deleteIfExists(decryptedFile);

            if (i % 10 == 0) {
                System.out.printf("  Completed %d/%d iterations%n", i, ITERATIONS);
            }
        }

        Files.deleteIfExists(inputFile);

        printStats("Encrypt   ", encryptLatencies);
        printStats("Decrypt   ", decryptLatencies);
        printStats("Round-trip", roundTripLatencies);
    }

    private void printStats(String label, List<Long> latencies) {
        Collections.sort(latencies);
        long min = latencies.get(0);
        long max = latencies.get(latencies.size() - 1);
        double avg = latencies.stream().mapToLong(Long::longValue).average().orElse(0);
        long p50 = percentile(latencies, 50);
        long p95 = percentile(latencies, 95);
        long p99 = percentile(latencies, 99);

        System.out.printf("%n=== %s latency (ms) over %d iterations ===%n", label, ITERATIONS);
        System.out.printf("  min=%-6d  avg=%-8.1f  p50=%-6d  p95=%-6d  p99=%-6d  max=%d%n",
                min, avg, p50, p95, p99, max);
    }

    private long percentile(List<Long> sorted, int pct) {
        int idx = (int) Math.ceil(pct / 100.0 * sorted.size()) - 1;
        return sorted.get(Math.max(0, Math.min(idx, sorted.size() - 1)));
    }
}
