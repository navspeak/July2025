import java.util.concurrent.locks.ReentrantLock;

public class _5LockInterruptiblyDemo {

    static final ReentrantLock lock = new ReentrantLock();

    public static void main(String[] args) throws Exception {

        // Thread-0 grabs the lock and holds it
        Thread holder = new Thread(() -> {
            lock.lock();
            try {
                System.out.println("Holder acquired lock");
                sleep(5000); // simulate long work
            } finally {
                lock.unlock();
                System.out.println("Holder released lock");
            }
        });

        // Thread-1 waits interruptibly
        Thread waiter = new Thread(() -> {
            try {
                System.out.println("Waiter trying lockInterruptibly...");
                lock.lockInterruptibly();
                try {
                    System.out.println("Waiter acquired lock");
                } finally {
                    lock.unlock();
                }
            } catch (InterruptedException e) {
                System.out.println("❌ Waiter interrupted while waiting for lock");
                Thread.currentThread().interrupt(); // restore
            }
        });

        holder.start();
        sleep(100);        // ensure holder acquires first
        waiter.start();

        sleep(1000);       // waiter is now blocked
        System.out.println(">>> Interrupting waiter");
        waiter.interrupt();
//
//        ☝️ Key observation
//        The waiter never acquires the lock.
//        The interrupt causes an immediate exit from the wait
//                | Method                | Blocks        | Interruptible |
//                | --------------------- | ------------- | ------------- |
//                | `lock()`              | Yes           | ❌ No          |
//                | `lockInterruptibly()` | Yes           | ✅ Yes         |
//                | `tryLock()`           | No            | ❌ No          |
//                | `tryLock(timeout)`    | Yes (bounded) | ✅ Yes         |

    }

    static void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException ignored) {}
    }
}
