package com.example.abuse;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class StreamAbuse {
    public static void main(String[] args) {
        example1();
        example_improvement();
    }



    private static void example1() {
        int[] nums = {1,2,2,3};
        Map<Integer, Integer> freq = new HashMap<>();
        Arrays.stream(nums)
//                .parallel() ->
                .boxed()
                .forEach(n -> freq.put(n, freq.getOrDefault(n, 0) + 1));
//                .forEach(n -> freq.merge(n, 0, Integer::sum));
        freq.entrySet().forEach(System.out::println);
        System.out.println("🚩 Stream anti-pattern detected:");
        System.out.println("• Using stream().forEach() with side effects (mutating external state).");
        System.out.println("• Dangerous if switched to parallel(): HashMap is not thread-safe.");
        System.out.println("• getOrDefault() + put() is non-atomic and will break under parallel streams.");
        System.out.println("• Boxing overhead (int → Integer) adds allocations with no functional gain.");
        System.out.println("• Prefer a plain loop or Map.merge() for clarity, safety, and performance.");
    }

    private static void example_improvement() {
        int[] nums = {1,2,2,3};
        Map<Integer, Integer> freq = new HashMap<>();
        // Unsafe
        Arrays.stream(nums).boxed()
                .forEach(n -> freq.put(n, freq.getOrDefault(n, 0) + 1));
        // Safer but still stream abuse
        Arrays.stream(nums).boxed()
                .forEach(n -> freq.merge(n, 1, Integer::sum));
        // Best
        for (int n : nums) {
            freq.merge(n, 1, Integer::sum);
        }

    }
}
