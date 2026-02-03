package com.example.multithreading;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class _8_5CFFailureAndRecovery {

    public static void main(String[] args) {

        // Toggle this to see success vs failure flow
        boolean failBase = true;

        CompletableFuture<Integer> base =
                CompletableFuture.supplyAsync(() -> {
                    log("base started");
                    sleep(150);
                    if (failBase) throw new IllegalStateException("boom in base");
                    return 10;
                });

        CompletableFuture<Integer> applied =
                base.thenApply(v -> {
                    log("thenApply got " + v);
                    // You can also fail here:
                    // if (v == 10) throw new RuntimeException("boom in thenApply");
                    return v * 2;
                });

        CompletableFuture<Void> accepted =
                applied.thenAccept(v -> {
                    log("thenAccept got " + v);
                    // You can also fail here:
                    // throw new RuntimeException("boom in thenAccept");
                });

        CompletableFuture<Void> run =
                accepted.thenRun(() -> log("thenRun after accept"));

        // 1) Observe success/failure (does NOT change outcome)
        CompletableFuture<Void> observed =
                accepted.whenComplete((ok, ex) -> {
                    if (ex != null) log("whenComplete saw FAILURE: " + root(ex));
                    else log("whenComplete saw SUCCESS");
                });

        // 2) Recover on failure (exceptionally runs only on failure)
        CompletableFuture<Void> recoveredWithExceptionally =
                observed.exceptionally(ex -> {
                    log("exceptionally fallback (only on failure): " + root(ex));
                    return null; // must return Void
                });

        // 3) Recover / transform in one place (handle runs on success OR failure)
        CompletableFuture<String> summary =
                applied.handle((val, ex) -> {
                    if (ex != null) return "SUMMARY: failed (" + root(ex) + ")";
                    return "SUMMARY: success value=" + val;
                });

        // Wait for completion (join throws CompletionException if still failing)
        log(summary.join());
        recoveredWithExceptionally.join();

        // join() vs get()
        demoJoinVsGet(base);
        /*
        thenApply, thenAccept, thenRun do not execute
            whenComplete does execute (it sees failure)
            exceptionally does execute (fallback)
            handle executes and produces a summary string
            join() throws CompletionException
            get() throws ExecutionException

            Want to observe but still fail the request? → whenComplete
            Want to recover and continue? → exceptionally or handle
            Want to recover but keep error info? → handle((v, ex) -> Result) style
         */
    }

    private static void demoJoinVsGet(CompletableFuture<Integer> base) {
        // join() throws unchecked CompletionException
        try {
            base.join();
        } catch (Exception e) {
            log("join threw: " + e.getClass().getSimpleName() + " root=" + root(e));
        }

        // get() throws checked ExecutionException / InterruptedException
        try {
            base.get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log("get interrupted");
        } catch (ExecutionException e) {
            log("get threw: " + e.getClass().getSimpleName() + " root=" + root(e));
        }
    }

    private static String root(Throwable t) {
        Throwable cur = t;
        while (cur.getCause() != null) cur = cur.getCause();
        return cur.getClass().getSimpleName() + ": " + cur.getMessage();
    }

    private static void log(String msg) {
        System.out.println("[" + Thread.currentThread().getName() + "] " + msg);
    }

    private static void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
    }
}
