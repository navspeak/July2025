package com.example.ratelimit;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Sliding Window Counter (O(1) memory) rate limiter.
 *
 * Approximates "requests in last W time" using:
 *   effective = currCount + prevCount * (1 - progress)
 * where progress = (now - currentWindowStart) / W  in [0,1).
 *
 * - Exact sliding window log: stores timestamps (O(N)), exact.
 * - Sliding window counter: stores only 2 counters (O(1)), approximate.
 */
public class _5SlidingWindowCounter {

    private final int capacity;              // max requests per window (approx)
    private final long windowSizeNanos;      // W in nanoseconds

    private volatile long currentWindowStart; // start of current window (aligned)
    private final AtomicInteger currCount = new AtomicInteger(0);
    private final AtomicInteger prevCount = new AtomicInteger(0);

    public _5SlidingWindowCounter(int capacity, long windowSizeMillis) {
        this.capacity = capacity;
        this.windowSizeNanos = TimeUnit.MILLISECONDS.toNanos(windowSizeMillis);

        long now = System.nanoTime();
        this.currentWindowStart = windowStart(now);
    }

    public boolean allow() {
        long now = System.nanoTime();
        long ws = windowStart(now);

        // roll window if needed (rare path)
        if (ws != currentWindowStart) {
            synchronized (this) {
                if (ws != currentWindowStart) {
                    long windowsPassed = (ws - currentWindowStart) / windowSizeNanos;

                    if (windowsPassed >= 2) {
                        // jumped over at least one full window -> discard old history
                        prevCount.set(0);
                        currCount.set(0);
                    } else {
                        // shift: current becomes previous
                        prevCount.set(currCount.get());
                        currCount.set(0);
                    }
                    currentWindowStart = ws;
                }
            }
        }

        // compute effective count in the last W duration (approx)
        double effective = effectiveCount(now);

        // enforce
        if (effective >= capacity) return false;

        currCount.incrementAndGet();
        return true;
    }

    private double effectiveCount(long nowNanos) {
        long ws = currentWindowStart; // volatile read

        double progress = (nowNanos - ws) / (double) windowSizeNanos; // 0..1
        if (progress < 0) progress = 0;
        if (progress > 1) progress = 1;

        double weightPrev = 1.0 - progress;
        return currCount.get() + prevCount.get() * weightPrev;
    }

    private long windowStart(long nowNanos) {
        return (nowNanos / windowSizeNanos) * windowSizeNanos;
    }

    // ------------------- Demo / Driver -------------------

    public static void main(String[] args) throws Exception {
        // 5 requests per ~1 second (approx)
        _5SlidingWindowCounter rl = new _5SlidingWindowCounter(5, 1000);

        System.out.println("Sliding Window Counter Demo (O(1) memory)");
        System.out.println("capacity=5, window=1000ms");
        System.out.println();

        // Burst 1
        System.out.println("Burst #1: 8 quick requests (expect ~5 allowed, rest blocked)");
        for (int i = 1; i <= 8; i++) {
            boolean allowed = rl.allow();
            System.out.printf("t=%dms %s -> req#%d %s%n",
                    shortNowMs(), rl.debug(), i, allowed ? "ALLOWED" : "BLOCKED");
            Thread.sleep(80);
        }

        // Partial overlap period
        System.out.println("\nSleep 600ms (previous window overlaps partially)...");
        Thread.sleep(600);

        System.out.println("Burst #2: 6 requests after 600ms (should gradually allow as overlap shrinks)");
        for (int i = 1; i <= 6; i++) {
            boolean allowed = rl.allow();
            System.out.printf("t=%dms %s -> req#%d %s%n",
                    shortNowMs(), rl.debug(), i, allowed ? "ALLOWED" : "BLOCKED");
            Thread.sleep(120);
        }

        System.out.println("\nNote: This is an approximation (unlike Sliding Window Log).");
    }

    private String debug() {
        long now = System.nanoTime();
        long ws = currentWindowStart;
        double progress = (now - ws) / (double) windowSizeNanos;
        if (progress < 0) progress = 0;
        if (progress > 1) progress = 1;

        double eff = effectiveCount(now);
        return String.format("prev=%d curr=%d progress=%.2f effective=%.2f",
                prevCount.get(), currCount.get(), progress, eff);
    }

    private static long shortNowMs() {
        // readable timestamps (last 100 seconds)
        return TimeUnit.NANOSECONDS.toMillis(System.nanoTime()) % 100_000;
    }
}