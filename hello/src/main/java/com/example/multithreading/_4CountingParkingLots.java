package com.example.multithreading;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/*
Problem statement (as interviewer would ask)
You are given a parking lot with N parking slots.
Cars (threads) arrive and try to park
If no slot is available, the car must wait
When a car leaves, a slot becomes free and one waiting car may park
Multiple cars can arrive and leave concurrently
👉 Implement this using Java concurrency primitives.
 */
public class _4CountingParkingLots {

    public static void main(String[] args) {
        ParkingLot lot = new ParkingLot(2);

        Runnable car = () -> {
            boolean parked = false;
            try {
                lot.park();
                parked = true;
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                if (parked) lot.leave();
            }
        };

        ExecutorService es = Executors.newFixedThreadPool(5);
        for (int i = 0; i < 5; i++) es.submit(car);

        es.shutdown();
        try {
            if (!es.awaitTermination(15, TimeUnit.SECONDS)) {
                es.shutdownNow();
            }
        } catch (InterruptedException e) {
            es.shutdownNow();
            Thread.currentThread().interrupt();
        }

        System.out.println("All cars done");
    }

    static class ParkingLot {
        private final Semaphore slots;

        ParkingLot(int capacity) {
            this.slots = new Semaphore(capacity, true);
        }

        void park() throws InterruptedException {
            System.out.println(Thread.currentThread().getName() + " trying to park");
            slots.acquire();
            // Below is non blocking
//            if (!slots.tryAcquire()) {
//                System.out.println(name + " waiting (lot full)");
//                slots.acquire();
//            }
            System.out.println(Thread.currentThread().getName() + " parked");
        }

        void leave() {
            slots.release();
            System.out.println(Thread.currentThread().getName() + " left");
        }
    }
}
