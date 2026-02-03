package com.example.multithreading;

import java.util.function.Consumer;

public class _1ProducerConsumer {
    public static void main(String[] args) throws InterruptedException {
        Buffer buffer = new Buffer();

        // 1. The Producer
        Thread p1 = new Thread(createProducer(buffer, 0, 600), "Producer");
        Thread c1 = new Thread(createConsumer(buffer, System.out::println), "Consumer-1");
        Thread c2 = new Thread(createConsumer(buffer, System.out::println), "Consumer-2");
        p1.start();
        c1.start();
        c2.start();

        p1.join();
        c1.join();
        c2.join();

        System.out.println("All work complete.");


    }

    public static class Buffer {
        private Integer value = null;
        private boolean closed = false;

        public synchronized void put(int val) throws InterruptedException {
            // why wait in while: spurious wakeup
            while (value != null){ //Full
                if (isClosed()){
                    throw new IllegalStateException("Buffer is Closed");
                }
                wait();
            }
            value = val;
            System.out.println(Thread.currentThread().getName() + "Produced " + value);
            notifyAll();
        }

        public synchronized Integer take() throws InterruptedException {
            if (isClosed()) throw new IllegalStateException("Buffer is closed");
                while (value == null){ //Empty
                wait();
                if (isClosed()) throw new IllegalStateException("Buffer is closed");
            }
            var ret = value;
            value = null;
            System.out.println(Thread.currentThread().getName() + "consumed " + ret);
            notifyAll();
            return ret;
        }

        public synchronized boolean isClosed() {
            // we could have removed synchronized if it was volatile
            return closed;
        }

        public synchronized void close() {
            this.closed = true;
            notifyAll();
        }
    }



    // Returns a Producer Task
    public static Runnable createProducer(Buffer buffer, int start, int end) {
        return () -> {
            try {
                for (int i = start; i < end; i++) {
                    buffer.put(i);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                // Optional: only close if this is the "final" producer
                // buffer.close();
            }
        };
    }

    // Returns a Consumer Task
    public static Runnable createConsumer(Buffer buffer, Consumer<Integer> consumer) {
        return () -> {
            try {
                while (true) {
                    Integer val = buffer.take();
                    if (val == null) break;
                    consumer.accept(val);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        };
    }

}
