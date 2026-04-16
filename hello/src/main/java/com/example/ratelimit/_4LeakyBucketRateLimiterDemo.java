package com.example.ratelimit;
import java.time.Instant;
import java.util.concurrent.TimeUnit;

/**
 * Strategy #4: Leaky Bucket Rate Limiting
 *
 * Mental model:
 * - Requests enter a bucket/queue (capacity = C).
 * - The bucket "leaks" (drains) at a constant rate: r requests/sec.
 * - If the bucket is full when a request arrives => BLOCK.
 *
 * This is like smoothing traffic: you can accept bursts only up to capacity,
 * but you process/drain at a steady rate.
 *
 * Math model (continuous):
 *   queued = max(0, queued - drainRate * Δt)
 *   allow if queued < capacity
 *   if allowed: queued = queued + 1
 *
 * Note
 * - This demo models only admission control (allow/block) using a "virtual queue size".
 * - Real systems often have an actual queue + a worker draining it.
 */
public class _4LeakyBucketRateLimiterDemo {

    static class LeakyBucketRateLimiter {
        private final double capacity;      // max queued requests allowed
        private final double drainPerMs;    // requests drained per millisecond

        private double queued;              // current queued size (can be fractional)
        private long lastUpdateMs;

        public LeakyBucketRateLimiter(double capacity, double drainPerSecond) {
            if (capacity <= 0 || drainPerSecond <= 0) {
                throw new IllegalArgumentException("capacity and drainPerSecond must be > 0");
            }
            this.capacity = capacity;
            this.drainPerMs = drainPerSecond / 1000.0;
            this.queued = 0.0;
            this.lastUpdateMs = Instant.now().toEpochMilli();
        }

        public boolean allowRequest() {
            long now = Instant.now().toEpochMilli();
            synchronized (this) {
                drain(now);

                if (queued + 1.0 <= capacity) {
                    queued += 1.0; // enqueue this request
                    return true;
                }
                return false;
            }
        }

        private void drain(long nowMs) {
            long elapsedMs = nowMs - lastUpdateMs;
            if (elapsedMs <= 0) return;

            double leaked = elapsedMs * drainPerMs;
            queued = Math.max(0.0, queued - leaked);
            lastUpdateMs = nowMs;
        }

        public synchronized double getQueued() {
            return queued;
        }
    }

    public static void main(String[] args) throws Exception {
        // Example:
        // capacity = 5 queued requests max
        // drain = 2 requests/sec (steady)
        LeakyBucketRateLimiter rl = new LeakyBucketRateLimiter(5, 2);

        System.out.println("Leaky Bucket Rate Limiter Demo");
        System.out.println("capacity=5 (queue), drain=2 req/sec (steady leak)");
        System.out.println();

        // Burst: 10 requests quickly. Expect ~5 allowed then blocked (depending on drain timing).
        System.out.println("Burst #1: 10 quick requests");
        for (int i = 1; i <= 10; i++) {
            boolean allowed = rl.allowRequest();
            System.out.printf("t=%dms queued=%.2f -> req#%d %s%n",
                    shortNowMs(), rl.getQueued(), i, allowed ? "ALLOWED" : "BLOCKED");
            TimeUnit.MILLISECONDS.sleep(80);
        }

        // Wait to let it leak/drain
        System.out.println("\nSleep 2 seconds (should drain ~4 requests)...");
        TimeUnit.SECONDS.sleep(2);

        System.out.println("Burst #2: 6 requests after drain");
        for (int i = 1; i <= 6; i++) {
            boolean allowed = rl.allowRequest();
            System.out.printf("t=%dms queued=%.2f -> req#%d %s%n",
                    shortNowMs(), rl.getQueued(), i, allowed ? "ALLOWED" : "BLOCKED");
            TimeUnit.MILLISECONDS.sleep(100);
        }

        System.out.println("\nObservation:");
        System.out.println("- Leaky bucket smooths traffic: steady drain rate.");
        System.out.println("- If input rate > drain for long, queue fills and blocks.");
    }

    private static long shortNowMs() {
        return Instant.now().toEpochMilli() % 100_000;
    }
}