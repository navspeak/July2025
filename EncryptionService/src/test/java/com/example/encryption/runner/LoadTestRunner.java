package com.example.encryption.runner;

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
 * Run with: mvn spring-boot:run -Dspring-boot.run.profiles=load -Dspring-boot.run.useTestClasspath=true
 */
@Component
@Profile("load")
public class LoadTestRunner implements CommandLineRunner {

    private static final int WARMUP          = 10;
    private static final int ITERATIONS      = 100;
    private static final int CONCURRENCY     = 10;
    private static final int FILE_SIZE_BYTES = 900 * 1024; // 900 KB — under 1 MB server limit

    private record TaskResult(long encMs, long decMs, long roundMs, String traceId, int encStatus, int decStatus) {}

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
            EncryptionResult enc = client.encrypt(f, "my-key", "AES_256_GCM");
            if (enc.path() != null) {
                EncryptionResult dec = client.decrypt(enc.path(), "my-key");
                if (dec.path() != null) Files.deleteIfExists(dec.path());
                Files.deleteIfExists(enc.path());
            }
            Files.deleteIfExists(f);
        }

        System.out.printf("Running %d iterations with concurrency=%d...%n", ITERATIONS, CONCURRENCY);

        List<Callable<TaskResult>> tasks = IntStream.range(0, ITERATIONS)
                .<Callable<TaskResult>>mapToObj(i -> () -> {
                    Path taskFile = Path.of("bench_" + i + ".bin");
                    Files.write(taskFile, data);
                    try {
                        long roundStart = System.nanoTime();

                        long t0 = System.nanoTime();
                        EncryptionResult enc = client.encrypt(taskFile, "my-key", "AES_256_GCM");
                        long encMs = elapsed(t0);

                        long decMs = 0;
                        int decStatus = -1;

                        if (enc.isSuccess() && enc.path() != null) {
                            long t1 = System.nanoTime();
                            EncryptionResult dec = client.decrypt(enc.path(), "my-key");
                            decMs = elapsed(t1);
                            decStatus = dec.status();
                            if (dec.path() != null) Files.deleteIfExists(dec.path());
                            Files.deleteIfExists(enc.path());
                        }

                        long roundMs = elapsed(roundStart);
                        return new TaskResult(encMs, decMs, roundMs, enc.traceId(), enc.status(), decStatus);
                    } finally {
                        Files.deleteIfExists(taskFile);
                    }
                })
                .toList();

        ExecutorService pool = Executors.newFixedThreadPool(CONCURRENCY);
        List<Future<TaskResult>> futures = pool.invokeAll(tasks);
        pool.shutdown();

        List<Long> encryptLatencies   = new ArrayList<>(ITERATIONS);
        List<Long> decryptLatencies   = new ArrayList<>(ITERATIONS);
        List<Long> roundTripLatencies = new ArrayList<>(ITERATIONS);
        List<TaskResult> failures     = new ArrayList<>();
        TaskResult slowest = null;

        for (Future<TaskResult> f : futures) {
            TaskResult r = f.get();
            if (r.encStatus() == 200) encryptLatencies.add(r.encMs());
            if (r.decStatus() == 200) decryptLatencies.add(r.decMs());
            if (r.encStatus() == 200 && r.decStatus() == 200) {
                roundTripLatencies.add(r.roundMs());
                if (slowest == null || r.roundMs() > slowest.roundMs()) slowest = r;
            }
            if (r.encStatus() != 200 || r.decStatus() != 200) failures.add(r);
        }

        printStats("Encrypt   ", encryptLatencies);
        printStats("Decrypt   ", decryptLatencies);
        printStats("Round-trip", roundTripLatencies);

        if (slowest != null) {
            System.out.printf("%nSlowest: traceId=%s  encMs=%d  decMs=%d  roundMs=%d%n",
                    slowest.traceId(), slowest.encMs(), slowest.decMs(), slowest.roundMs());
        }

        if (!failures.isEmpty()) {
            System.out.printf("%nNon-200 responses (%d / %d):%n", failures.size(), ITERATIONS);
            failures.forEach(r -> System.out.printf("  traceId=%s  encStatus=%d  decStatus=%d%n",
                    r.traceId(), r.encStatus(), r.decStatus()));
        }
    }

    private long elapsed(long startNano) {
        return (System.nanoTime() - startNano) / 1_000_000;
    }

    private void printStats(String label, List<Long> latencies) {
        if (latencies.isEmpty()) {
            System.out.printf("%n=== %s — no successful samples ===%n", label);
            return;
        }
        Collections.sort(latencies);
        long min = latencies.get(0);
        long max = latencies.get(latencies.size() - 1);
        double avg = latencies.stream().mapToLong(Long::longValue).average().orElse(0);
        long p50 = percentile(latencies, 50);
        long p95 = percentile(latencies, 95);
        long p99 = percentile(latencies, 99);

        System.out.printf("%n=== %s latency (ms) over %d samples, concurrency=%d ===%n",
                label, latencies.size(), CONCURRENCY);
        System.out.printf("  min=%-6d  avg=%-8.1f  p50=%-6d  p95=%-6d  p99=%-6d  max=%d%n",
                min, avg, p50, p95, p99, max);
    }

    private long percentile(List<Long> sorted, int pct) {
        int idx = (int) Math.ceil(pct / 100.0 * sorted.size()) - 1;
        return sorted.get(Math.max(0, Math.min(idx, sorted.size() - 1)));
    }
}
