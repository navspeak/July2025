package com.example.multithreading;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/*
Alternate printing with wait/notify
Two threads:
- Thread A prints: A1 A2 A3 ... A10
- Thread B prints: B1 B2 B3 ... B10
Output must be strictly alternating:
- A1 B1 A2 B2 ... A10 B10
Rules
    Must use synchronized + wait/notify
    No sleep hacks
    Use a shared “turn” flag

 */
public class _2AlternatePrinting {
    public static void main(String[] args) throws InterruptedException {
        Printer printer = new Printer();
        Thread tA = new Thread(printer::printA);
        Thread tB = new Thread(printer::printB);
        tA.start();
        tB.start();
        tA.join();
        tB.join();
    }

    public static class Printer {
        private int current = 1;
        private String turn = "A";

        public synchronized void printA() {
            try {
                while (current < 11) {
                    while (!turn.equals("A")) {
                        wait();
                    }
                    System.out.println("A" + current);
                    turn = "B";
                    notify();
                }
            } catch (InterruptedException e) {
               Thread.currentThread().interrupt();
            }
        }

        public synchronized void printB() {
            try {
                while (current < 11) {
                    while (!turn.equals("B")) {
                        wait();
                    }
                    System.out.println("B" + current++);
                    turn = "A";
                    notify();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }



}
