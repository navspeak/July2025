package com.example.multithreading;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class _3AQSDeadlockSubtleMitigated {

    static final ReentrantReadWriteLock rw = new ReentrantReadWriteLock(false); // non-fair like your dump

    // How long to keep the program alive (so you have time)
    static final long RUN_FOR_MS = TimeUnit.MINUTES.toMillis(3);

    // How long writers hold the write lock each time (creates visible waiting)
    static final long WRITE_HOLD_MS = 5_000;

    // How long readers “work” under read lock
    static final long READ_WORK_MS = 200;

    public static void main(String[] args) throws InterruptedException {

        long endAt = System.currentTimeMillis() + RUN_FOR_MS;

        Runnable safeReadThenMaybeWrite = () -> {
            while (System.currentTimeMillis() < endAt) {

                // 1) Acquire read lock (normal)
                // All 6 will acquire = readCount  = 6
                rw.readLock().lock();
                boolean needWrite;
                try {
                    sleep(READ_WORK_MS);
                    // In real code, this is some condition discovered during read
                    needWrite = true;
                } finally {
                    rw.readLock().unlock(); // all 6 will unlock, eventually readCount = 0
                }

                // 2) FIX: release read lock BEFORE taking write lock, then re-check
                // for READ_WORK_MS no one will get write lock. Once readCount = 0
                // say T3 gets write lock, other 5 in WAITING(parking)
                if (needWrite) {
                    rw.writeLock().lock();
                    try {
                        // Hold write lock long enough so others queue up (visible contention)
                        sleep(WRITE_HOLD_MS);
                    } finally {
                        rw.writeLock().unlock();
                    }
                }

                // small pause so threads alternate and you see parking/unparking
                sleep(50);
            }
        };

        // More threads => more obvious contention in the dump
        List<Thread> threads = new ArrayList<>();
        for (int i = 1; i <= 6; i++) {
            threads.add(new Thread(safeReadThenMaybeWrite, "T" + i));
        }

        threads.forEach(Thread::start);

        // Wait for all to finish; after ~3 mins the JVM exits
        for (Thread t : threads) {
            t.join();
        }

        System.out.println("Done. Exiting cleanly.");
    }

    static void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
