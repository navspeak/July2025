package com.example.multithreading;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

public class _8_3CompletableFuture {
    public static void main(String[] args) {
        ExecutorService es = Executors.newFixedThreadPool(
                5,
                new NamedThreadFactory("pricing-pool")
        );

        System.out.println("=== submit 6 CF tasks ===");

        CompletableFuture<?>[] futures = IntStream.range(0, 6)
                .mapToObj(taskId -> CompletableFuture
                        .supplyAsync(() -> work(taskId), es)
                        .thenAccept(result ->
                                System.out.println("Completed result = " + result
                                        + " (handled by " + Thread.currentThread().getName() + ")")
                        )
                        .exceptionally(ex -> {
                            System.out.println("A task failed: " + unwrap(ex));
                            return null;
                        })
                )
                .toArray(CompletableFuture[]::new);
//        CompletableFuture<Integer>
//        → thenAccept
//        CompletableFuture<Void>

        // Wait for all to finish (including handling callbacks)
        CompletableFuture.allOf(futures).join();

        es.shutdown();
    }

    static int work(int taskId) {
        String thread = Thread.currentThread().getName();

        long delayMs = switch (taskId) {
            case 0 -> 900;
            case 1 -> 100;
            case 2 -> 600;
            case 3 -> 200;   // will fail quickly
            case 4 -> 400;
            default -> 150;
        };

        System.out.println("Task " + taskId + " started on " + thread + " (delay " + delayMs + "ms)");

        sleep(delayMs);

        if (taskId == 3) {
            throw new IllegalStateException("Boom from task " + taskId);
        }
        return taskId * 10;
    }

    static void sleep(long ms) {
        try { Thread.sleep(ms); }
        catch (InterruptedException e) { Thread.currentThread().interrupt(); }
    }

    static Throwable unwrap(Throwable ex) {
        // exceptionally receives CompletionException/ExecutionException wrappers
        if (ex instanceof CompletionException && ex.getCause() != null) return ex.getCause();
        return ex;
    }

    static class NamedThreadFactory implements ThreadFactory {
        private final String poolName;
        private final AtomicInteger counter = new AtomicInteger(1);

        NamedThreadFactory(String poolName) { this.poolName = poolName; }

        @Override
        public Thread newThread(Runnable r) {
            Thread t = new Thread(r);
            t.setName(poolName + "-thread-" + counter.getAndIncrement());
            return t;
        }
    }
}
/*
6️⃣ Why join() throws CompletionException but get() does not
cf.get();   // throws ExecutionException (checked)
cf.join();  // throws CompletionException (unchecked)


Why?

get() matches Future contract (old, blocking, checked)
join() matches functional style (modern, unchecked)

📌 This is deliberate API design.

7️⃣ What you’ll see in callbacks
cf.exceptionally(ex -> {
    // ex is almost always CompletionException
    Throwable root = ex.getCause();
    ...
});


So this helper is common:

static Throwable unwrap(Throwable ex) {
    if (ex instanceof CompletionException && ex.getCause() != null)
        return ex.getCause();
    if (ex instanceof ExecutionException && ex.getCause() != null)
        return ex.getCause();
    return ex;
}

get() forces try catch behaviour
try {
    Integer r = cf.get();
} catch (ExecutionException e) {
    System.out.println(e.getCause());
} catch (InterruptedException e) {
    Thread.currentThread().interrupt();
}

Cleaner, but:

try {
    cf.join();
} catch (CompletionException e) { // unchecked
    System.out.println(e.getCause());
}


Why join() exists at all

Because functional chains hate checked exceptions.

Imagine this with get():

list.stream()
    .map(cf -> {
        try {
            return cf.get();
        } catch (...) { ... }
    });

 list.stream()
    .map(CompletableFuture::join)
    .toList();

5️⃣ Interruption semantics (subtle but important)
get()

If interrupted → throws InterruptedException

Clears interrupt flag

You must re-interrupt manually

join()

Does not throw InterruptedException

If interrupted:

continues waiting

restores interrupt flag before returning

So after join():

Thread.currentThread().isInterrupted() == true


This is intentional: join() is not cancellation-aware.

6️⃣ Timeouts

Both support timeouts, but:

cf.get(2, SECONDS);     // checked TimeoutException
cf.orTimeout(2, SECONDS).join(); // CompletionException wrapping TimeoutException


Modern CF style prefers the second.

7️⃣ When to use which (real-world guidance)
Use join() when:

You are already in CF land

You’re inside streams / lambdas

You want clean code

You’re aggregating many futures

Use get() when:

You are implementing a blocking API

You must propagate InterruptedException

You are at a low-level concurrency boundary

8️⃣ Production pitfall (common)
CompletableFuture.allOf(futures).join();


If one future failed:

join() throws CompletionException

cause is first failure only

others may also have failed silently

Fix:

handle exceptions per future

or inspect each outcome

9️⃣ Interview answer (polished)

“get() and join() both block, but get() follows the old Future contract with checked exceptions, while join() is designed
 for CompletableFuture pipelines and throws unchecked CompletionException.
 In async and stream-heavy code, I prefer join(); at blocking boundaries I may still use get().”

Case 4️⃣ Production-safe pattern (capture ALL outcome)

This is the best practice.

record Outcome<T>(T value, Throwable error) {}

CompletableFuture<Outcome<Integer>> safe =
    CompletableFuture.supplyAsync(this::callApi)
        .thenApply(v -> new Outcome<>(v, null))
        .exceptionally(ex -> new Outcome<>(null, unwrap(ex)));

 */