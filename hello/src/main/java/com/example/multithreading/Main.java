package com.example.multithreading;

import java.util.concurrent.CompletableFuture;

public class Main {
    public static void main(String[] args) {
        CompletableFuture<Integer> base
                = CompletableFuture.supplyAsync(() -> work(1));

        CompletableFuture<Integer> applied = base.thenApply(value -> {
            System.out.println("thenApply got: " + value);
            return value * 2; // transforms
        });


        CompletableFuture<Void> accepted =
                applied.thenAccept(value -> {
                    System.out.println("thenAccept got: " + value);
                });

        CompletableFuture<Void> run =
                accepted.thenRun(() -> {
                    System.out.println("thenRun: no input, just running");
                });

        run.join();
        System.out.println("Done");
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
}
