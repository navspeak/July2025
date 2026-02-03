package com.example.multithreading;

import lombok.SneakyThrows;

import java.util.*;

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
public class _2AlternatePrinting_ABC {

    public static void main(String[] args) throws InterruptedException {
        Printer printer = new Printer(10);
        Thread tA = new Thread(()->printer.print('A'), "A");
        Thread tB = new Thread(()->printer.print('B'), "B");
        Thread tC = new Thread(()->printer.print('C'), "C");
        tA.start();
        tB.start();
        tC.start();

        tA.join();
        tB.join();
        tC.join();
    }

    public static class Printer {
        private int current = 1;
        private int max = 10;
        private int turn = 1;
        private Map<Character, Integer> position = Map.of('A', 1, 'B', 2, 'C', 3);

        public Printer(int max) {
            this.max = max;
        }

        public synchronized void print(char c) {
            int pos = position.getOrDefault(c, -1);
            if (pos == -1) throw new UnsupportedOperationException();

            try {
                while (current < max) {
                    while (pos != turn){
                        wait();
                    }
                    System.out.println(c + "" +current);
                    if (turn == position.size()) current++;
                    turn = (turn + 1) % position.size() + 1;
                    notifyAll();
                }
            } catch (InterruptedException e) {
               Thread.currentThread().interrupt();
            }
        }
    }



}
