package com.example.multithreading;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class _8_1ExecutoryServiceExecuteSubmit {
    public static void main(String[] args) {
        ExecutorService es = Executors.newFixedThreadPool(5, new NamedThreadFactory("my-ppol"));
        Runnable runnableTask = () -> {
            System.out.println(
                    "Running in " + Thread.currentThread().getName()
            );
        };
        System.out.println("=== execute() ===");

        for (int i = 1; i <= 3; i++) {
            es.execute(runnableTask); // can't accept calables
        }
//        ✔ No Future
//        ✔ Exceptions go straight to the thread

        System.out.println("=== submit() ===");

        for (int i = 4; i <= 6; i++) {
            Future<?> future = es.submit(() -> {
                System.out.println(
                        "Running in " + Thread.currentThread().getName()
                );
            });

            // optional: wait or observe
            try {
                future.get();
            } catch (Exception e) {
                System.out.println("Exception caught: " + e);
            }
        }
        es.shutdown();

//        Only 5 threads created (fixed pool)
//        6th task waits until one thread frees up
//        Thread names clearly visible
//        execute() has no lifecycle handle
//        submit() gives observability
    }

    public static class NamedThreadFactory implements ThreadFactory{
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
