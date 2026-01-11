package com.example.brushup;

import java.util.stream.IntStream;

public class Loops {
    public static void main(String[] args) {
        Integer[] array = IntStream.range(1, 10_000_001).boxed().toArray(Integer[]::new);
        int[] res = new int[10_000_000];
        long startTimeFor = System.nanoTime();

        for (int i = 0; i < 10_000_000; i++) {
            // Square each number and store it in the result array
            res[i] = array[i] * array[i];
        }

        long endTimeFor = System.nanoTime();
        long durationFor = endTimeFor - startTimeFor;
        System.out.println(durationFor);
    }
}
