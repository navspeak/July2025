package com.example.ratelimit;

public class _4LeakyBucket {
    private final int capacity;          // max queue size
    private final double drainPerNano;   // requests drained per nanosecond

    private double queued;               // current queue size (can be fractional)
    private long lastUpdateTime;         // last time we drained

    public _4LeakyBucket(int capacity, int drainRatePerSecond) {
        this.capacity = capacity;

        // convert to per-nanosecond rate
        this.drainPerNano = drainRatePerSecond / 1_000_000_000.0;

        this.queued = 0.0;
        this.lastUpdateTime = System.nanoTime();
    }

    public boolean allow(){
        var now = System.nanoTime();
        var elapsedTime = now - lastUpdateTime;

        if (elapsedTime > 0){
            double leaked = elapsedTime * drainPerNano; // how many requests drained
            queued = Math.max(0.0, queued - leaked);     // queue can't go below 0
            lastUpdateTime = now;                        // IMPORTANT
        }

        // admission control
        if (queued + 1.0 <= capacity) {
            queued += 1.0;                               // enqueue this request
            return true;
        }
        return false;
    }
}
