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
 * Pass mode as first argument via -Dspring-boot.run.arguments=<mode>:
 *   roundtrip (default) — single encrypt→decrypt cycle, prints latency
 *   load                — concurrent encrypt→decrypt, prints percentile stats
 */
@Component
@Profile("load")
public class EncryptionBenchmarkRunner implements CommandLineRunner {

    private record TaskResult(long encMs, long decMs, String traceId, int encStatus, int decStatus) {}

    private final FileEncryptionClient client;
    private final RunnerProperties.Load load;

    public EncryptionBenchmarkRunner(FileEncryptionClient client, RunnerProperties props) {
        this.client = client;
        this.load = props.load();
    }

    @Override
    public void run(String... args) throws Exception {
        String mode = args.length > 0 ? args[0].toLowerCase() : "roundtrip";
        if ("load".equals(mode)) {
            runLoadTest();
        } else {
            runRoundTrip();
        }
    }

    private void runRoundTrip() throws Exception {
        byte[] data = new byte[load.fileSizeKb() * 1024];
        new Random().nextBytes(data);

        Path inputFile = Path.of("roundtrip_test.bin");
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
                if (dec.path() != null) Files.deleteIfExists(dec.path());
                Files.deleteIfExists(enc.path());
            }

            System.out.printf("%n=== Round-trip (file=%d KB) ===%n", load.fileSizeKb());
            System.out.printf("  encrypt=%d ms  decrypt=%d ms  total=%d ms%n",
                    encMs, decMs, encMs + decMs);
            System.out.printf("  traceId=%s  encStatus=%d  decStatus=%s%n",
                    enc.traceId(), enc.status(), dec != null ? dec.status() : "-");
        } finally {
            Files.deleteIfExists(inputFile);
        }
    }

    private void runLoadTest() throws Exception {
        byte[] data = new byte[load.fileSizeKb() * 1024];
        new Random().nextBytes(data);

        System.out.printf("Warming up with %d sequential iterations...%n", load.warmup());
        for (int i = 0; i < load.warmup(); i++) {
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

        System.out.printf("Running %d iterations with concurrency=%d, fileSize=%d KB...%n",
                load.iterations(), load.concurrency(), load.fileSizeKb());

        List<Callable<TaskResult>> tasks = IntStream.range(0, load.iterations())
                .<Callable<TaskResult>>mapToObj(i -> () -> {
                    Path taskFile = Path.of("bench_" + i + ".bin");
                    Files.write(taskFile, data);
                    try {
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

                        return new TaskResult(encMs, decMs, enc.traceId(), enc.status(), decStatus);
                    } finally {
                        Files.deleteIfExists(taskFile);
                    }
                })
                .toList();

        ExecutorService pool = Executors.newFixedThreadPool(load.concurrency());
        List<Future<TaskResult>> futures = pool.invokeAll(tasks);
        pool.shutdown();

        List<Long> encryptLatencies = new ArrayList<>(load.iterations());
        List<Long> decryptLatencies = new ArrayList<>(load.iterations());
        List<TaskResult> failures = new ArrayList<>();

        for (Future<TaskResult> f : futures) {
            TaskResult r = f.get();
            if (r.encStatus() == 200) encryptLatencies.add(r.encMs());
            if (r.decStatus() == 200) decryptLatencies.add(r.decMs());
            if (r.encStatus() != 200 || r.decStatus() != 200) failures.add(r);
        }

        printStats("Encrypt", encryptLatencies);
        printStats("Decrypt", decryptLatencies);

        if (!failures.isEmpty()) {
            System.out.printf("%nNon-200 responses (%d / %d):%n", failures.size(), load.iterations());
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
                label, latencies.size(), load.concurrency());
        System.out.printf("  min=%-6d  avg=%-8.1f  p50=%-6d  p95=%-6d  p99=%-6d  max=%d%n",
                min, avg, p50, p95, p99, max);
    }

    private long percentile(List<Long> sorted, int pct) {
        int idx = (int) Math.ceil(pct / 100.0 * sorted.size()) - 1;
        return sorted.get(Math.max(0, Math.min(idx, sorted.size() - 1)));
    }
}
