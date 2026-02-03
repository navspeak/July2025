package com.example.multithreading;

import java.util.concurrent.*;
/*
Exercise: Make 3 worker threads wait for each other before starting work—run once with CountDownLatch
(works once, can’t reset) and once with CyclicBarrier (resets and repeats every round).
 */
public class _6LatchVsBarrier {
    public static void main(String[] args) throws Exception {

        System.out.println("\n=== CountDownLatch (one-shot) ===");
        CountDownLatch latch = new CountDownLatch(3);
        for (int i = 1; i <= 3; i++) {
            int id = i;
            new Thread(() -> {
                sleep(300 * id);
                System.out.println("Worker " + id + " done");
                latch.countDown();
            }).start();
        }
        latch.await();
        System.out.println("MAIN continues (cannot reuse latch)");

        System.out.println("\n=== CyclicBarrier (reusable) ===");
        CyclicBarrier barrier = new CyclicBarrier(3,
                () -> System.out.println(">> All reached barrier, next round"));

        Runnable task = () -> {
            for (int round = 1; round <= 2; round++) {
                System.out.println(Thread.currentThread().getName() +
                        " reached barrier (round " + round + ")");
                try {
                    barrier.await();
                } catch (Exception e) {
                    return;
                }
            }
        };

        for (int i = 0; i < 3; i++) {
            new Thread(task, "T" + i).start();
        }
    }

    static void sleep(long ms) {
        try { Thread.sleep(ms); } catch (InterruptedException ignored) {}
    }
}

