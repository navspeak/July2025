package com.example.multithreading;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

public class _8_7FanoutBoundedConcurrency {

    public static void main(String[] args) {
        ExecutorService apiPool = Executors.newFixedThreadPool(3, namedFactory("api-")); // bounded concurrency = 3


        List<CompletableFuture<Result<String>>> futures  = IntStream.range(0, 6)
                .mapToObj(taskId ->
                        CompletableFuture.supplyAsync(() -> callRemoteApi(taskId), apiPool)
                                // Convert success/failure into a Result so the pipeline doesn't short-circuit
                                .handle((val, ex) -> ex == null
                                        ? Result.<String>ok(taskId, val)
                                        : Result.<String>fail(taskId, root(ex)))
                )
                .toList();

        // Wait for ALL (these futures never complete exceptionally because handle() swallows into Result)
        CompletableFuture<Void> all = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
        all.join();

        // Gather results
        List<Result<String>> results = futures.stream().map(CompletableFuture::join).toList();

        // Print summary
        long okCount = results.stream().filter(Result::isOk).count();
        long failCount = results.size() - okCount;

        System.out.println("\n=== SUMMARY ===");
        System.out.println("Total=" + results.size() + " ok=" + okCount + " failed=" + failCount);

        System.out.println("\nSuccesses:");
        results.stream().filter(Result::isOk).forEach(r ->
                System.out.println("  task " + r.taskId + " -> " + r.value)
        );

        System.out.println("\nFailures:");
        results.stream().filter(r -> !r.isOk()).forEach(r ->
                System.out.println("  task " + r.taskId + " -> " + r.error)
        );

        apiPool.shutdown();
    }

    // Simulated remote API call: some succeed, some fail
    static String callRemoteApi(int taskId) {
        log("calling remote api for task " + taskId);
        sleep(200 + taskId * 50L);

        // Fail a few deterministically for demo
        if (taskId == 1 || taskId == 3 || taskId == 5) {
            throw new RuntimeException("HTTP 500 from downstream for task " + taskId);
        }
        return "OK(" + taskId + ")";
    }

    // Result wrapper (keeps error info without failing whole batch)
    static final class Result<T> {
        final int taskId;
        final T value;
        final String error;

        private Result(int taskId, T value, String error) {
            this.taskId = taskId;
            this.value = value;
            this.error = error;
        }

        static <T> Result<T> ok(int taskId, T value) {
            return new Result<>(taskId, value, null);
        }

        static <T> Result<T> fail(int taskId, String error) {
            return new Result<>(taskId, null, error);
        }

        boolean isOk() {
            return error == null;
        }
    }

    // ---------- helpers ----------
    static ThreadFactory namedFactory(String prefix) {
        AtomicInteger seq = new AtomicInteger(1);
        return r -> {
            Thread t = new Thread(r);
            t.setName(prefix + seq.getAndIncrement());
            return t;
        };
    }

    static void log(String msg) {
        System.out.println("[" + Thread.currentThread().getName() + "] " + msg);
    }

    static void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
    }

    static String root(Throwable t) {
        Throwable cur = t;
        while (cur.getCause() != null) cur = cur.getCause();
        return cur.getClass().getSimpleName() + ": " + cur.getMessage();
    }
}

