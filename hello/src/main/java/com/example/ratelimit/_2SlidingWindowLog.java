package com.example.ratelimit;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class _2SlidingWindowLog {
    private final int capacity;
    private final long windowSizeNanos;
    private final Queue<Long> log = new ArrayDeque<>();

    public _2SlidingWindowLog(int capacity, long windowSizeMillis) {
        this.capacity = capacity;
        this.windowSizeNanos = windowSizeMillis * 1_000_000L;
    }

    public synchronized boolean allow() {
        long now = System.nanoTime();
        long windowStart = now - windowSizeNanos;

        while (!log.isEmpty() && log.peek() <= windowStart) {
            log.remove();
        }

        if (log.size() < capacity) {
            log.add(now);
            return true;
        }
        return false;
    }
}