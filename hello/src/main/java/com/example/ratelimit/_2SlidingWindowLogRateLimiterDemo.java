package com.example.ratelimit;

import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.TimeUnit;

/**
 * Strategy #2: Sliding Window Log Rate Limiting
 *
 * Idea:
 * - Maintain a log (timestamps) of *accepted* requests.
 * - For a given request at time "now":
 *     1) Remove timestamps older than (now - windowSize).
 *     2) If remaining count < limit => allow and add "now"
 *        else => block.
 *
 * Pros:
 * - Accurate (no boundary burst problem like Fixed Window).
 * - Easy to reason about.
 *
 * Cons:
 * - Memory cost: stores 1 timestamp per allowed request in the window.
 * - Under very high QPS, can be heavy.
 *
 * Best for:
 * - Smaller limits or where precision matters.
 */
public class _2SlidingWindowLogRateLimiterDemo {

    // -------------------- Rate Limiter --------------------

    static class SlidingWindowLogRateLimiter {
        private final int limit;
        private final long windowSizeMillis;

        // Store timestamps of ALLOWED requests (in millis)
        private final Deque<Long> timestamps = new ArrayDeque<>();

        public SlidingWindowLogRateLimiter(int limit, long windowSizeMillis) {
            this.limit = limit;
            this.windowSizeMillis = windowSizeMillis;
        }

        /**
         * @return true if allowed, false if blocked
         */
        public boolean allowRequest() {
            long now = Instant.now().toEpochMilli();
            long windowStart = now - windowSizeMillis;

            // Remove old timestamps outside the sliding window
            synchronized (this) {
                while (!timestamps.isEmpty() && timestamps.peekFirst() <= windowStart) {
                    timestamps.removeFirst();
                }

                if (timestamps.size() < limit) {
                    timestamps.addLast(now);
                    return true;
                }
                return false;
            }
        }

        public synchronized int getCurrentCountInWindow() {
            return timestamps.size();
        }
    }

    // -------------------- Driver / Demo --------------------

    public static void main(String[] args) throws Exception {
        SlidingWindowLogRateLimiter rl = new SlidingWindowLogRateLimiter(5, 1_000);

        System.out.println("Sliding Window Log Rate Limiter Demo");
        System.out.println("Limit = 5 requests per 1 second (moving window)");
        System.out.println();

        // Burst: 8 requests quickly. Should allow first 5, block next 3 (like fixed window),
        // but the key difference is: there is no boundary burst when crossing the second boundary.
        System.out.println("Burst #1: 8 requests quickly (expect 5 allowed, 3 blocked)");
        for (int i = 1; i <= 8; i++) {
            boolean allowed = rl.allowRequest();
            System.out.printf(
                    "t=%dms inWindow=%d -> req#%d %s%n",
                    shortNowMs(), rl.getCurrentCountInWindow(), i, allowed ? "ALLOWED" : "BLOCKED"
            );
            TimeUnit.MILLISECONDS.sleep(80);
        }

        // Now demonstrate "smoothness":
        // Wait a bit (but not a full second) and retry: as timestamps expire, requests get allowed again.
        System.out.println("\nWait 500ms, then try 3 more (some may still be blocked depending on timing)...");
        TimeUnit.MILLISECONDS.sleep(500);

        for (int i = 1; i <= 3; i++) {
            boolean allowed = rl.allowRequest();
            System.out.printf(
                    "t=%dms inWindow=%d -> afterWait req#%d %s%n",
                    shortNowMs(), rl.getCurrentCountInWindow(), i, allowed ? "ALLOWED" : "BLOCKED"
            );
            TimeUnit.MILLISECONDS.sleep(100);
        }

        // Wait long enough so the entire previous 1s window clears
        System.out.println("\nWait 1.2s to clear the window, then 6 requests (expect 5 allowed, 1 blocked)...");
        TimeUnit.MILLISECONDS.sleep(1_200);

        for (int i = 1; i <= 6; i++) {
            boolean allowed = rl.allowRequest();
            System.out.printf(
                    "t=%dms inWindow=%d -> finalBurst req#%d %s%n",
                    shortNowMs(), rl.getCurrentCountInWindow(), i, allowed ? "ALLOWED" : "BLOCKED"
            );
            TimeUnit.MILLISECONDS.sleep(60);
        }

        System.out.println("\nNote: Unlike Fixed Window, you can't 'cheat' by hitting the boundary.");
    }

    private static long shortNowMs() {
        // Only for readable logs (last 100 seconds)
        return Instant.now().toEpochMilli() % 100_000;
    }
}