package com.example.multithreading;

import java.util.concurrent.*;
import java.util.concurrent.locks.*;

public class _7RWLockDemos {

    public static void main(String[] args) throws Exception {
        demoReadersParallel();
        demoWriterBlocksReaders();
        demoDowngradeWriteToRead();
        demoUpgradeReadToWriteDeadlockRisk();
    }

    // 1) Many readers can hold readLock concurrently
    static void demoReadersParallel() throws Exception {
        System.out.println("\n=== 1) Readers parallel ===");
        ReadWriteLock rw = new ReentrantReadWriteLock(true);

        ExecutorService pool = Executors.newFixedThreadPool(4);
        for (int i = 1; i <= 3; i++) {
            int id = i;
            pool.submit(() -> {
                rw.readLock().lock();
                try {
                    System.out.println(ts() + " R" + id + " acquired readLock");
                    Thread.sleep(400);
                    System.out.println(ts() + " R" + id + " releasing readLock");
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    rw.readLock().unlock();
                }
            });
        }

        pool.shutdown();
        pool.awaitTermination(2, TimeUnit.SECONDS);
    }

    // 2) A writer blocks all readers while holding writeLock
    static void demoWriterBlocksReaders() throws Exception {
        System.out.println("\n=== 2) Writer blocks readers ===");
        ReadWriteLock rw = new ReentrantReadWriteLock(true);
        ExecutorService pool = Executors.newFixedThreadPool(4);

        // reader first (gets in)
        pool.submit(readerTask(rw, "R1", 600));

        // writer shortly after (will wait until R1 releases)
        pool.submit(() -> {
            sleep(100);
            rw.writeLock().lock();
            try {
                System.out.println(ts() + " W acquired writeLock (exclusive)");
                Thread.sleep(500);
                System.out.println(ts() + " W releasing writeLock");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                rw.writeLock().unlock();
            }
        });

        // reader after writer starts waiting (should block until writer done)
        pool.submit(() -> {
            sleep(200);
            readerTask(rw, "R2", 200).run();
        });

        pool.shutdown();
        pool.awaitTermination(3, TimeUnit.SECONDS);
    }

    // 3) Downgrade: thread holding writeLock can acquire readLock (write -> read)
    static void demoDowngradeWriteToRead() throws Exception {
        System.out.println("\n=== 3) Downgrade (write -> read) works ===");
        ReentrantReadWriteLock rw = new ReentrantReadWriteLock(true);

        Thread t = new Thread(() -> {
            rw.writeLock().lock();
            try {
                System.out.println(ts() + " T acquired writeLock");
                // "downgrade": acquire readLock while still holding writeLock
                rw.readLock().lock();
                System.out.println(ts() + " T also acquired readLock (downgrade OK)");
            } finally {
                rw.writeLock().unlock();
                System.out.println(ts() + " T released writeLock (still holds readLock)");
            }

            try {
                sleep(300);
            } finally {
                rw.readLock().unlock();
                System.out.println(ts() + " T released readLock");
            }
        });

        t.start();
        t.join();
    }

    // 4) Upgrade: readLock -> writeLock is dangerous (may deadlock)
    static void demoUpgradeReadToWriteDeadlockRisk() throws Exception {
        System.out.println("\n=== 4) Upgrade (read -> write) deadlock risk demo ===");
        ReentrantReadWriteLock rw = new ReentrantReadWriteLock(true);

        Thread upgrader = new Thread(() -> {
            rw.readLock().lock();
            try {
                System.out.println(ts() + " U acquired readLock, now trying to upgrade to writeLock...");
                // Try lock with timeout so program doesn't hang forever
                boolean gotWrite = rw.writeLock().tryLock(500, TimeUnit.MILLISECONDS);
                if (!gotWrite) {
                    System.out.println(ts() + " U could NOT acquire writeLock while holding readLock (upgrade blocked)");
                } else {
                    try {
                        System.out.println(ts() + " U upgraded (this is not a safe pattern generally)");
                    } finally {
                        rw.writeLock().unlock();
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                rw.readLock().unlock();
                System.out.println(ts() + " U released readLock");
            }
        });

        // Add a second reader to make the upgrade obviously impossible
        Thread otherReader = new Thread(() -> {
            rw.readLock().lock();
            try {
                System.out.println(ts() + " R acquired readLock (kept for a bit)");
                sleep(800);
                System.out.println(ts() + " R releasing readLock");
            } finally {
                rw.readLock().unlock();
            }
        });

        otherReader.start();
        Thread.sleep(50);
        upgrader.start();

        upgrader.join();
        otherReader.join();

        System.out.println(ts() + " Done (notice upgrade couldn't happen)");
    }

    // helpers
    static Runnable readerTask(ReadWriteLock rw, String name, long holdMs) {
        return () -> {
            rw.readLock().lock();
            try {
                System.out.println(ts() + " " + name + " acquired readLock");
                Thread.sleep(holdMs);
                System.out.println(ts() + " " + name + " releasing readLock");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                rw.readLock().unlock();
            }
        };
    }

    static void sleep(long ms) {
        try { Thread.sleep(ms); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
    }

    static String ts() {
        return String.format("[%d]", System.currentTimeMillis() % 100000);
    }

    public static class Main {
        public static void main(String[] args) {
            Lock lock = new ReentrantLock();
            lock.lock();


        }
    }
}
