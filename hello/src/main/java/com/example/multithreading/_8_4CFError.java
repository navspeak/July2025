package com.example.multithreading;

public class _8_4CFError {
    /*
    CompletableFuture<Integer> base = ...
    CompletableFuture<Integer> applied = base.thenApply(...)
    CompletableFuture<Void> accepted = applied.thenAccept(...)
    CompletableFuture<Void> run = accepted.thenRun(...)
------------------
1) If base fails
    thenApply does not run
    thenAccept does not run
    thenRun does not run

run.join() throws CompletionException (wrapping the root cause)

2) If thenApply throws (applied fails)
    thenAccept does not run
    thenRun does not run

run.join() throws CompletionException

3) If thenAccept throws (accepted fails)
    thenRun does not run

run.join() throws CompletionException

4) If thenRun throws
 run.join() throws CompletionException
---------------
    So: any failure at any step stops the remaining “thenX” steps.

How to run something even if there’s a failure
    Use one of these:

    whenComplete((value, ex) -> ...) (observe/log, doesn’t change the value)
            run.whenComplete((v, ex) -> {
        if (ex != null) System.out.println("Failed: " + ex);
        else System.out.println("Succeeded");
    }).join();

    handle((value, ex) -> ...) (convert failure into a value)
    CompletableFuture<Integer> safe =
            base.handle((v, ex) -> ex != null ? -1 : v);

System.out.println(safe.join()); // never throws (in this example)
    exceptionally(ex -> fallback) (fallback value, only on failure)
    CompletableFuture<Integer> recovered =
            base.exceptionally(ex -> 0);

     */
}
