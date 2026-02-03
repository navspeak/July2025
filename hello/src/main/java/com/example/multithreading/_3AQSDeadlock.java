package com.example.multithreading;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
public class _3AQSDeadlock {

    private static final Lock L1 = new ReentrantLock();
    private static final Lock L2 = new ReentrantLock();

    public static void main(String[] args) throws Exception {
        Thread tA = new Thread(() -> {
            L1.lock();
            try {
                sleep(100);         // widen the race window
                L2.lock();               // <-- waits here forever
                try {
                    System.out.println("A acquired both");
                } finally {
                    L2.unlock();
                }
            } finally {
                L1.unlock();
            }
        }, "Thread-A");

        Thread tB = new Thread(() -> {
            L2.lock();
            try {
                sleep(100);
                L1.lock();               // <-- waits here forever
                try {
                    System.out.println("B acquired both");
                } finally {
                    L1.unlock();
                }
            } finally {
                L2.unlock();
            }
        }, "Thread-B");

        tA.start();
        tB.start();

        tA.join();
        tB.join();
    }

    private static void sleep(long ms) {
        try { Thread.sleep(ms); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
    }
}
/*
Lock L1 (AQS)
────────────────────────────────────────────────────────
state: 1  (locked)
owner: Thread-A

CLH queue:
HEAD ──> [Node(Thread-B) | WAITING (parking)] ──> TAIL
          ^ Thread-B is parked here waiting for L1


Lock L2 (AQS)
────────────────────────────────────────────────────────
state: 1  (locked)
owner: Thread-B

CLH queue:
HEAD ──> [Node(Thread-A) | WAITING (parking)] ──> TAIL
          ^ Thread-A is parked here waiting for L2

Cycle (why it never resolves)
Thread-A: owns L1  → waits for L2 (but L2 owned by Thread-B)
Thread-B: owns L2  → waits for L1 (but L1 owned by Thread-A)


So each lock’s AQS queue has the “other” thread parked at/near the head,
but the owner can’t release because it’s waiting on the other lock.
 */

/*
With AQS (ReentrantLock, semaphores, latches, etc.) a thread dump usually shows “WAITING (parking)”,
which is ambiguous in a single snapshot.

What one jstack can tell you:
It can tell you:
- Thread states right now
- Where each thread is parked (Unsafe.park → LockSupport.park → AQS.acquire)
- Which stack frame in your code called lock() / await() / etc.

But with AQS, one dump usually cannot prove permanence.
Why it’s ambiguous with AQS:
In a single dump, these two scenarios look almost identical:
Normal contention
- Thread is waiting because another thread holds the lock
- A moment later, owner releases → waiter proceeds

Deadlock (circular wait)
- Thread is waiting for a lock held by another thread that will never release (because it’s also waiting)
Both show up as:
-WAITING (parking)
AbstractQueuedSynchronizer.acquire(...)
ReentrantLock.lock(...)

Why it is often enough for synchronized

With monitor deadlocks (synchronized), jstack often prints:

Found one Java-level deadlock:
and it also gives the monitor ownership chain. That’s definitive from one dump.

AQS deadlocks don’t always trigger that JVM deadlock detector.

The real diagnostic trick

Take 2–3 dumps, 10–15 seconds apart:

If the same threads are parked

at the same code line (com.yourapp...)

with no progress across dumps

…then it’s “stuck forever” behavior (deadlock or infinite wait). That time dimension is what one dump lacks.

*/