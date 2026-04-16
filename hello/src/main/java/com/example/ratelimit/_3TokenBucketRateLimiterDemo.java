package com.example.ratelimit;

import java.time.Instant;
import java.util.concurrent.TimeUnit;

/**
 * Strategy #3: Token Bucket Rate Limiting
 *
 * Mental model:
 * - You have a bucket that holds tokens (capacity C).
 * - Tokens are added continuously at a fixed rate (refillRate tokens/sec).
 * - Each request consumes 1 token.
 * - If there's a token -> ALLOW and decrement.
 * - If bucket is empty -> BLOCK.
 *
 * Pros:
 * - Smooth average rate + allows short bursts (up to capacity).
 * - Great for APIs, user traffic, microservices.
 *
 * Cons:
 * - Slightly more complex than fixed/sliding window.
 *
 * Key terms:
 * - capacity: max tokens stored (burst size)
 * - refillRate: tokens added per second (sustained rate)
 */
public class _3TokenBucketRateLimiterDemo {

    // -------------------- Rate Limiter --------------------

    static class TokenBucketRateLimiter {
        private final long capacity;          // max tokens (burst)
        private final double refillPerMs;     // tokens per millisecond

        private double tokens;               // current tokens (can be fractional)
        private long lastRefillTimeMs;        // epoch millis

        public TokenBucketRateLimiter(long capacity, long refillTokensPerSecond) {
            if (capacity <= 0 || refillTokensPerSecond <= 0) {
                throw new IllegalArgumentException("capacity and refillTokensPerSecond must be > 0");
            }
            this.capacity = capacity;
            this.refillPerMs = refillTokensPerSecond / 1000.0;
            this.tokens = capacity; // start full (common choice)
            this.lastRefillTimeMs = Instant.now().toEpochMilli();
        }

        /**
         * Try to consume 1 token.
         * @return true if allowed, false if rate-limited
         */
        public boolean allowRequest() {
            long now = Instant.now().toEpochMilli();

            synchronized (this) {
                refill(now);

                if (tokens >= 1.0) {
                    tokens -= 1.0;
                    return true;
                }
                return false;
            }
        }

        private void refill(long nowMs) {
            long elapsedMs = nowMs - lastRefillTimeMs;
            if (elapsedMs <= 0) return; // System time is not guaranteed monotonic.
            // System.nanoTime() => is always monotonic increasing

            double added = elapsedMs * refillPerMs;
            tokens = Math.min(capacity, tokens + added);
            lastRefillTimeMs = nowMs;
        }

        public synchronized double getTokens() {
            return tokens;
        }
    }

    // -------------------- Driver / Demo --------------------

    public static void main(String[] args) throws Exception {
        // capacity = 5 tokens (burst up to 5)
        // refill rate = 2 tokens/sec (sustained average)
        TokenBucketRateLimiter rl = new TokenBucketRateLimiter(5, 2);

        System.out.println("Token Bucket Rate Limiter Demo");
        System.out.println("capacity=5 (burst), refillRate=2 tokens/sec (sustained)");
        System.out.println();

        // Burst: try 8 immediately (should allow 5 then block 3)
        System.out.println("Burst #1: 8 immediate requests (expect 5 allowed, 3 blocked)");
        for (int i = 1; i <= 8; i++) {
            boolean allowed = rl.allowRequest();
            System.out.printf(
                    "t=%dms tokens=%.2f -> req#%d %s%n",
                    shortNowMs(), rl.getTokens(), i, allowed ? "ALLOWED" : "BLOCKED"
            );
            TimeUnit.MILLISECONDS.sleep(50);
        }

        // Wait 1 second: should refill about 2 tokens
        System.out.println("\nSleep 1s (should refill ~2 tokens)...");
        TimeUnit.SECONDS.sleep(1);

        // Try 4 more: likely allow ~2 then block remaining depending on timing
        System.out.println("Burst #2: 4 requests after 1s");
        for (int i = 1; i <= 4; i++) {
            boolean allowed = rl.allowRequest();
            System.out.printf(
                    "t=%dms tokens=%.2f -> afterWait req#%d %s%n",
                    shortNowMs(), rl.getTokens(), i, allowed ? "ALLOWED" : "BLOCKED"
            );
            TimeUnit.MILLISECONDS.sleep(80);
        }

        System.out.println("\nObservation:");
        System.out.println("- You can burst up to capacity (5).");
        System.out.println("- Then you must 'wait' for tokens to refill (2/sec).");
    }

    private static long shortNowMs() {
        // only for readable logs
        return Instant.now().toEpochMilli() % 100_000;
    }
}
