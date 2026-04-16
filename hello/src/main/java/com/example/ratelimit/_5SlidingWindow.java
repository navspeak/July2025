package com.example.ratelimit;

public class _5SlidingWindow {
    private int capacity;
    private long windowSizeInNanoSecs;

    private long windowStart;
    private int currCount;
    private int prevCount;
    // [0–10), [10–20), [20–30), [30–40) ...

    public _5SlidingWindow(int capacity, int windowSizeInMin) {
        this.capacity = capacity;
        this.windowSizeInNanoSecs = windowSizeInMin * 60 * 1_000_000_000L;
        this.windowStart = (System.nanoTime() / windowSizeInNanoSecs) * windowSizeInNanoSecs;
        this.currCount = 0;
        this.prevCount = 0;
    }

    public boolean allow(){
        long now = System.nanoTime(); // assume currTime is 23 => go to bucket [20-30)
        long currWindow = (now/windowSizeInNanoSecs) * windowSizeInNanoSecs; // 23/10*10=20 => [20-30)

        //Window changed
        if (currWindow != windowStart){ // now >= windowStart + windowSizeInNanoSecs
            long diff = currWindow - windowStart;
            if (diff >= 2 * windowSizeInNanoSecs) {
                // jumped over at least one whole window; previous bucket no longer relevant
                prevCount = 0;
            } else {
                prevCount = currCount; // shift current -> previous
            }
            currCount = 0;
            windowStart = currWindow;
        }

        long elapsed = now - windowStart; // [0..windowSize)
        double currWt = (double)elapsed/windowSizeInNanoSecs; // Important - no int divis
        double effective = (1.0-currWt)*prevCount + currCount;
        if (effective < capacity){
            currCount++;
            return true;
        }
        return false;
    }
}
