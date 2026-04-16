package com.example.ratelimit;


import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAdder;

public class _1FixedWindow {
    private final int capacity;
    private final long windowSizeNanos;
    private final AtomicLong timeWindow = new AtomicLong();
    private final AtomicInteger count = new AtomicInteger(0);


    public _1FixedWindow(int capacity, int windowSizeInMillis) {
        this.capacity = capacity;
        this.windowSizeNanos = windowSizeInMillis * 1_000_000L;
        long now = System.nanoTime();
        /* say now = 128100, currWindow = floor(128100/1000) * 1000 = 128000
                        200
                        999
                     129000                                                  129000
         */
        this.timeWindow.set((now/windowSizeInMillis) * windowSizeNanos);
    }

    public boolean allowRequest(){
        long now = System.nanoTime();
        long currTimeWindow = (now/windowSizeNanos) * windowSizeNanos;
        if (currTimeWindow != timeWindow.get()){
            if (timeWindow.compareAndSet(currTimeWindow, currTimeWindow)){
                count.set(0);
            }
        }
        /*
        if (currTimeWindow != timeWindow) {
    synchronized (this) {
        if (currTimeWindow != timeWindow) {
            timeWindow = ws;
            count.set(0);
        }
    }
}
 */

        return count.incrementAndGet() <= capacity;
    }
}
