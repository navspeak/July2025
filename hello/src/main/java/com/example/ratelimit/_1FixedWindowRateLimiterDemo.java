package com.example.ratelimit;

import java.time.Instant;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Strategy #1: Fixed Window Counter Rate Limiting
 *
 * Idea:
 * - Divide time into fixed windows of size W (e.g., 1 second).
 * - For each window, maintain a counter of how many requests have happened.
 * - Allow up to LIMIT requests per window.
 * - When time moves into the next window, reset counter to 0.
 *
 * Pros:
 * - Very simple, fast, low memory.
 *
 * Cons (important):
 * - "Boundary burst" problem:
 *   If LIMIT=5 per second, a client can do:
 *     5 requests at 12:00:00.999
 *     5 requests at 12:00:01.001
 *   => effectively 10 requests in ~2ms.
 *
 * Best for:
 * - Simple APIs where approximate limiting is fine.
 */
public class _1FixedWindowRateLimiterDemo {

    // -------------------- Rate Limiter --------------------

    static class FixedWindowRateLimiter {
        private final int limit;                 // max requests per window
        private final long windowSizeMillis;      // window size, e.g. 1000ms

        // State for current window
        private volatile long currentWindowStart; // epoch millis
        private final AtomicInteger counter = new AtomicInteger(0);

        public FixedWindowRateLimiter(int limit, long windowSizeMillis) {
            this.limit = limit;
            this.windowSizeMillis = windowSizeMillis;
            this.currentWindowStart = windowStart(Instant.now().toEpochMilli());
        }

        /**
         * Try to acquire permission for 1 request.
         * @return true if allowed, false if rate-limited
         */
        public boolean allowRequest() {
            long now = Instant.now().toEpochMilli();
            long windowStart = windowStart(now);

            // If we moved into a new window, reset counter (safely).
            if (windowStart != currentWindowStart) {
                // A tiny synchronization to avoid partial resets under concurrency.
                // (For a demo: good enough. In production: consider LongAdder + CAS loops, etc.)
                synchronized (this) {
                    if (windowStart != currentWindowStart) {
                        currentWindowStart = windowStart;
                        counter.set(0);
                    }
                }
            }

            int newValue = counter.incrementAndGet();
            return newValue <= limit;
        }

        private long windowStart(long epochMillis) {
            // ... | 11,000–11,999 | 12,000–12,999 | 13,000–13,999 | ...
            //                       ↑
            //                    current window (if now = 12,345)
            return (epochMillis / windowSizeMillis) * windowSizeMillis;
        }

        public long getCurrentWindowStart() {
            return currentWindowStart;
        }

        public int getCurrentCount() {
            return counter.get();
        }
    }

    // -------------------- Driver / Demo --------------------

    public static void main(String[] args) throws Exception {
        // Example: allow 5 requests per 1 second fixed window
        FixedWindowRateLimiter rl = new FixedWindowRateLimiter(5, 1000);

        System.out.println("Fixed Window Rate Limiter Demo");
        System.out.println("Limit = 5 requests / 1 second");
        System.out.println();

        // Burst 1: 8 requests quickly in the same window
        System.out.println("Burst #1: 8 requests quickly (expect 5 allowed, 3 blocked)");
        for (int i = 1; i <= 8; i++) {
            boolean allowed = rl.allowRequest();
            System.out.printf(
                    "t=%dms windowStart=%d count=%d -> req#%d %s%n",
                    nowMs(), rl.getCurrentWindowStart(), rl.getCurrentCount(),
                    i, allowed ? "ALLOWED" : "BLOCKED"
            );
            TimeUnit.MILLISECONDS.sleep(80);
        }

        // Wait to ensure we cross into a new window
        System.out.println("\nSleeping ~1.2s to move to a new window...\n");
        TimeUnit.MILLISECONDS.sleep(1200);

        // Burst 2: show reset on new window
        System.out.println("Burst #2: 6 requests (expect 5 allowed, 1 blocked)");
        for (int i = 1; i <= 6; i++) {
            boolean allowed = rl.allowRequest();
            System.out.printf(
                    "t=%dms windowStart=%d count=%d -> req#%d %s%n",
                    nowMs(), rl.getCurrentWindowStart(), rl.getCurrentCount(),
                    i, allowed ? "ALLOWED" : "BLOCKED"
            );
            TimeUnit.MILLISECONDS.sleep(70);
        }

        // Optional: show boundary burst effect
        System.out.println("\nBoundary Burst Illustration:");
        System.out.println("If you time requests right at the end of a window and start of next, you can exceed the intended smooth rate.");
        System.out.println("Next strategies (Sliding Window / Token Bucket / Leaky Bucket) mitigate this.\n");
    }

    private static long nowMs() {
        // Just relative-ish output (epoch is fine too). Keeping it simple.
        return Instant.now().toEpochMilli() % 1_00_000;
    }
}