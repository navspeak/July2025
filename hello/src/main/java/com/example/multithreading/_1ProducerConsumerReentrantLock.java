package com.example.multithreading;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Queue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;

public class _1ProducerConsumerReentrantLock {
    public static void main(String[] args) throws InterruptedException {
        Buffer buffer = new Buffer(10);

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
        private final Queue<Integer> queue;
        private final int cap;
        private boolean closed = false;
        private Lock lock = new ReentrantLock();
        /*
        Rule for Condition naming
        --------------------------
            A Condition should be named after the predicate that becomes true when it is signaled.
            Not the action.
            Not the state you’re waiting to leave.
            The logical condition that must hold to proceed.
            Can I read condition.await() as an English sentence?
         */
        private Condition notFull = lock.newCondition();
        private Condition notEmpty = lock.newCondition();

        public Buffer(int size) {
            this.queue = new ArrayDeque<>(size);
            cap = size;
        }

        public void put(int val) throws InterruptedException {
            // why wait in while: spurious wakeup
            lock.lock();
            try {
                while (this.queue.size() == cap){ //Full
                    if (closed){
                        throw new IllegalStateException("Buffer is Closed");
                    }
                    notFull.await(); // wait for not full
                }
                queue.offer(val);
                notEmpty.signalAll();
            } finally {
                lock.unlock();
            }
        }

        public Integer take() throws InterruptedException {
            lock.lock();
            try {
                while (this.queue.isEmpty()){ //Empty
                    notEmpty.await();
                    if (closed) return null;
                }
                var ret = queue.poll();
                notFull.signalAll();
                return ret;
            } finally {
                lock.unlock();
            }
        }


        public void close() {
            lock.lock();
            try {
                this.closed = true;
                notFull.signalAll();
                notEmpty.signalAll();
            } finally {
                lock.unlock();
            }
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
    public static Runnable createConsumer(Buffer buffer, Consumer<String> consumer) {
        return () -> {
            try {
                while (true) {
                    Integer val = buffer.take();
                    if (val == null) break;
                    consumer.accept(Thread.currentThread().getName() + " consumed " + val);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        };
    }

}
