package com.example.ratelimit;

public class _3TokenBucket {
    private final int capacity;
    private final double refillPerNano;   // tokens per nanosecond

    private double tokens;
    private long lastRefillTime;

    // last computed retry-after (only meaningful when last call was rejected)
    private long lastRetryAfterNanos;

    public _3TokenBucket(int capacity, int refillRatePerMin) {
        this.capacity = capacity;
        this.refillPerNano = refillRatePerMin / (60*1_000_000_000.0);

        this.tokens = capacity; // start full
        this.lastRefillTime = System.nanoTime();
        this.lastRetryAfterNanos = 0L;
    }

    public synchronized boolean allow() {
        long now = System.nanoTime();
        refill(now);

        if (tokens >= 1.0) {        // ✅ important - can be fractional
            tokens -= 1.0;
            return true;
        }

        double shortfall = 1.0 - tokens; // (0, 1]
        // if refill rate is 10 per min. shortfall is 1
        // in 10 tokens => 1 mins
        //     1 token in 1/10 mins
        long nanos = (long) Math.ceil(shortfall / refillPerNano);
        lastRetryAfterNanos = Math.max(0L, nanos);
        return false;
    }

    public synchronized long getLastRetryAfterNanos() {
        return lastRetryAfterNanos;
    }

    private void refill(long now) {
        long elapsedTime = now - lastRefillTime;
        if (elapsedTime < 0) return;
        double tokensToAdd = elapsedTime * refillPerNano;
        tokens = Math.min(capacity, tokens + tokensToAdd);
        lastRefillTime = now;   // ✅ important
    }
}
