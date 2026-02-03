package com.example.multithreading;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class _8_2ExecutorServiceCallable {
    public static void main(String[] args) {
        executorCallable(); // head of line blocking
        executorCompletionService(); // no head of line blocking

    }

    private static void executorCompletionService() {
        ExecutorService es = Executors.newFixedThreadPool(
                5,
                new NamedThreadFactory("pricing-pool")
        );

        CompletionService<Integer> cs = new ExecutorCompletionService<>(es);

        System.out.println("=== submit 6 Callables ===");
        for (int i = 0; i < 6; i++) {
            int taskId = i;

            cs.submit(() -> {
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

                Thread.sleep(delayMs);

                if (taskId == 3) {
                    throw new IllegalStateException("Boom from task " + taskId);
                }

                return taskId * 10;
            });
        }

        System.out.println("\n=== take() results as they complete ===");
        for (int i = 0; i < 6; i++) {
            try {
                Future<Integer> f = cs.take();      // blocks until *any* task completes
                Integer result = f.get();           // unwrap result (or exception)
                System.out.println("Completed result = " + result);
            } catch (ExecutionException e) {
                System.out.println("A task failed: " + e.getCause());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }

        es.shutdown();
    }

    private static void executorCallable() {
        ExecutorService es = Executors.newFixedThreadPool(
                5,
                new NamedThreadFactory("pricing-pool")
        );

        System.out.println("=== submit(Callable) ===");

        Future<Integer>[] futures = new Future[6];

        for (int i = 0; i < 6; i++) {
            int taskId = i;

            futures[i] = es.submit(() -> {
                String thread = Thread.currentThread().getName();
                System.out.println("Task " + taskId + " running on " + thread);

                if (taskId == 3) {
                    throw new IllegalStateException("Boom from task " + taskId);
                }

                Thread.sleep(500); // simulate work
                return taskId * 10;
            });
        }

        System.out.println("\n=== Collect results ===");
        //Head-of-line blocking
        for (int i = 0; i < 6; i++) {
            try {
                System.out.println("Result: " + futures[i].get());
            } catch (ExecutionException e) {
                System.out.println("Task failed: " + e.getCause()); //Exception here
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        es.shutdown();
    }

    static class NamedThreadFactory implements ThreadFactory {
        private final String poolName;
        private final AtomicInteger counter = new AtomicInteger(1);

        NamedThreadFactory(String poolName) {
            this.poolName = poolName;
        }

        @Override
        public Thread newThread(Runnable r) {
            Thread t = new Thread(r);
            t.setName(poolName + "-thread-" + counter.getAndIncrement());
            return t;
        }
    }

}
