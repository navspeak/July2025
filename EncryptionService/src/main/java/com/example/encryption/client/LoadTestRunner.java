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
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.IntStream;

/**
 * Run with: --spring.profiles.active=load
 */
@Component
@Profile("load")
public class LoadTestRunner implements CommandLineRunner {

    private static final int WARMUP      = 10;
    private static final int ITERATIONS  = 100;
    private static final int CONCURRENCY = 10;
    private static final int FILE_SIZE_BYTES = 900 * 1024; // 900 KB — under 1 MB server limit

    private final FileEncryptionClient client;

    public LoadTestRunner(FileEncryptionClient client) {
        this.client = client;
    }

    @Override
    public void run(String... args) throws Exception {
        byte[] data = new byte[FILE_SIZE_BYTES];
        new Random().nextBytes(data);

        System.out.printf("Warming up with %d sequential iterations...%n", WARMUP);
        for (int i = 0; i < WARMUP; i++) {
            Path f = Path.of("warmup_" + i + ".bin");
            Files.write(f, data);
            Path enc = client.encrypt(f, "my-key", "AES_256_GCM");
            Path dec = client.decrypt(enc, "my-key");
            Files.deleteIfExists(enc);
            Files.deleteIfExists(dec);
            Files.deleteIfExists(f);
        }

        System.out.printf("Running %d iterations with concurrency=%d...%n", ITERATIONS, CONCURRENCY);

        List<Callable<long[]>> tasks = IntStream.range(0, ITERATIONS)
                .<Callable<long[]>>mapToObj(i -> () -> {
                    Path taskFile = Path.of("bench_" + i + ".bin");
                    Files.write(taskFile, data);
                    try {
                        long roundStart = System.nanoTime();

                        long t0 = System.nanoTime();
                        Path enc = client.encrypt(taskFile, "my-key", "AES_256_GCM");
                        long encMs = elapsed(t0);

                        long t1 = System.nanoTime();
                        Path dec = client.decrypt(enc, "my-key");
                        long decMs = elapsed(t1);

                        long roundMs = elapsed(roundStart);

                        Files.deleteIfExists(enc);
                        Files.deleteIfExists(dec);
                        return new long[]{encMs, decMs, roundMs};
                    } finally {
                        Files.deleteIfExists(taskFile);
                    }
                })
                .toList();

        ExecutorService pool = Executors.newFixedThreadPool(CONCURRENCY);
        List<Future<long[]>> futures = pool.invokeAll(tasks);
        pool.shutdown();

        List<Long> encryptLatencies   = new ArrayList<>(ITERATIONS);
        List<Long> decryptLatencies   = new ArrayList<>(ITERATIONS);
        List<Long> roundTripLatencies = new ArrayList<>(ITERATIONS);

        for (Future<long[]> f : futures) {
            long[] result = f.get();
            encryptLatencies.add(result[0]);
            decryptLatencies.add(result[1]);
            roundTripLatencies.add(result[2]);
        }

        printStats("Encrypt   ", encryptLatencies);
        printStats("Decrypt   ", decryptLatencies);
        printStats("Round-trip", roundTripLatencies);
    }

    private long elapsed(long startNano) {
        return (System.nanoTime() - startNano) / 1_000_000;
    }

    private void printStats(String label, List<Long> latencies) {
        Collections.sort(latencies);
        long min = latencies.get(0);
        long max = latencies.get(latencies.size() - 1);
        double avg = latencies.stream().mapToLong(Long::longValue).average().orElse(0);
        long p50 = percentile(latencies, 50);
        long p95 = percentile(latencies, 95);
        long p99 = percentile(latencies, 99);

        System.out.printf("%n=== %s latency (ms) over %d iterations, concurrency=%d ===%n",
                label, ITERATIONS, CONCURRENCY);
        System.out.printf("  min=%-6d  avg=%-8.1f  p50=%-6d  p95=%-6d  p99=%-6d  max=%d%n",
                min, avg, p50, p95, p99, max);
    }

    private long percentile(List<Long> sorted, int pct) {
        int idx = (int) Math.ceil(pct / 100.0 * sorted.size()) - 1;
        return sorted.get(Math.max(0, Math.min(idx, sorted.size() - 1)));
    }
}
