package com.example.multithreading;
public class _9ThreadLocalExample {

    static ThreadLocal<Integer> counter =
            ThreadLocal.withInitial(() -> 0);

    static void work() {
        int val = counter.get();   // thread-specific
        val++;
        counter.set(val);

        System.out.println(
                Thread.currentThread().getName() +
                        " counter=" + val
        );
    }

    public static void main(String[] args) {
        Runnable task = () -> {
            work();
            work();
        };

        new Thread(task, "T1").start();
        new Thread(task, "T2").start();
    }
}
