package com.example.multithreading;
import java.util.concurrent.locks.ReentrantReadWriteLock;
/*Now, here’s the subtle AQS deadlock example (very common in real systems) and the fix pattern.
Subtle AQS deadlock: Read-lock → write-lock upgrade (ReadWriteLock)
This bites people because it’s not “two locks”, it’s one read-write lock.
The bug
A thread holds the read lock and then tries to acquire the write lock without releasing read first.
If multiple threads do this, nobody can upgrade because write lock requires no readers.
*/

public class _3AQSDeadlockSubtle {
    static final ReentrantReadWriteLock rw = new ReentrantReadWriteLock();

    public static void main(String[] args) {
        Runnable task = () -> {
            rw.readLock().lock();
            try {
                // read something...
                sleep(100);

                // ❌ upgrade attempt while still holding read lock
                rw.writeLock().lock();
                /* Good Review Comment:
                This code attempts to acquire a write lock while holding a read lock.  A thread holding the read lock
                cannot safely acquire the write lock without first releasing the read lock.
                Please release the read lock first and re-check state under the write lock.”
                 */
                try {
                    // write something...
                } finally {
                    rw.writeLock().unlock();
                }
            } finally {
                rw.readLock().unlock();
            }
        };

        new Thread(task, "T1").start();
        new Thread(task, "T2").start();
    }

    static void sleep(long ms) {
        try {
            Thread.sleep(ms);
        }
        catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
/*
What happens (deadlock)

T1 holds read lock, tries write lock → waits
T2 holds read lock, tries write lock → waits

Write lock cannot be granted while any read lock is held

But both readers won’t release (they’re blocked trying to get write)
➡️ system stuck

RW Lock - READ side
────────────────────────────────────────
Readers holding: T1, T2 (readCount > 0)

RW Lock - WRITE side (AQS)
────────────────────────────────────────
owner: none
but cannot acquire because readCount > 0

CLH queue (writers):
HEAD ─> [Node(T1) WAITING] ─> [Node(T2) WAITING] ─> TAIL
        ^ both parked trying to acquire write lock

Cycle:
T1, T2 keep read locks held → prevents writer acquire
T1, T2 are parked waiting for write → never reach finally to release read


 */
