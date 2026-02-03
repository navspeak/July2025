package com.example.multithreading;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class _8_6CF_ThreadControl {
/*
What you should notice (important rules)
Rule A — thenApply (non-Async)
    Usually runs on the thread that completed the previous stage.
    So if base runs on io-pool-1, you’ll often see:
    base work on io-pool-1
    thenApply also on io-pool-1

Rule B — thenApplyAsync (no executor)
    Usually runs on ForkJoinPool.commonPool (threads like ForkJoinPool.commonPool-worker-*).
Rule C — thenApplyAsync(..., executor)
    Runs on your executor (io-pool-*), giving you control and predictable debugging.

Rule D — whenComplete vs whenCompleteAsync
    whenComplete runs on the thread that completes the stage (often same thread)
    whenCompleteAsync schedules observation asynchronously (and can be pinned to an executor)
 */
    public static void main(String[] args) {
        ExecutorService ioPool = Executors.newFixedThreadPool(
                2, namedFactory("io-pool-")
        );

        log("main start");

        CompletableFuture<Integer> base =
                CompletableFuture.supplyAsync(() -> {
                    log("base work");
                    sleep(200);
                    return 10;
                }, ioPool); // force base onto ioPool

        // 1) Non-Async: usually runs on the thread that completed the previous stage
        CompletableFuture<Integer> thenApply =
                base.thenApply(v -> {
                    log("thenApply (non-async) got " + v);
                    sleep(100);
                    return v * 2;
                });

        // 2) Async without executor: goes to ForkJoin common pool (usually)
        CompletableFuture<Integer> thenApplyAsyncCommon =
                thenApply.thenApplyAsync(v -> {
                    log("thenApplyAsync (common pool) got " + v);
                    sleep(100);
                    return v + 1;
                });

        // 3) Async with executor: force onto ioPool (or any pool you choose)
        CompletableFuture<Integer> thenApplyAsyncOnIo =
                thenApplyAsyncCommon.thenApplyAsync(v -> {
                    log("thenApplyAsync (ioPool) got " + v);
                    sleep(100);
                    return v * 10;
                }, ioPool);

        // Observe completion: where does this run?
        CompletableFuture<Integer> observed =
                thenApplyAsyncOnIo.whenComplete((val, ex) -> {
                    if (ex != null) log("whenComplete saw failure: " + root(ex));
                    else log("whenComplete saw success: val=" + val);
                });

        // whenCompleteAsync: you can control observer thread too
        CompletableFuture<Integer> observedAsync =
                observed.whenCompleteAsync((val, ex) -> {
                    log("whenCompleteAsync running (forced on ioPool)");
                }, ioPool);

        int finalValue = observedAsync.join();
        log("finalValue=" + finalValue);

        ioPool.shutdown();
        log("main end");
    }

    // ---------- helpers ----------
    static ThreadFactory namedFactory(String prefix) {
        AtomicInteger seq = new AtomicInteger(1);
        return r -> {
            Thread t = new Thread(r);
            t.setName(prefix + seq.getAndIncrement());
            t.setDaemon(false);
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

