package com.example.algo.abuse;

import org.junit.jupiter.api.RepeatedTest;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class _1_StreamAbuseTest {
    @RepeatedTest(500)
    void hashMap_parallel_put_getOrDefault_eventually_breaks() {
        int[] nums = makeLotsOfNumbers(200_000, 100); // 200k items, values 0..99

        Map<Integer, Integer> freq = new HashMap<>();

        // ❌ Anti-pattern: parallel stream + side effects + HashMap + non-atomic update
        Arrays.stream(nums)
                .boxed()
                .forEach(n -> freq.put(n, freq.getOrDefault(n, 0) + 1));

        int expectedTotal = nums.length;
        int actualTotal = freq.values().stream().mapToInt(Integer::intValue).sum();

        // If no race happened this time, totals match.
        // If race happened, actualTotal is often LESS than expectedTotal due to lost updates.
        assertEquals(expectedTotal, actualTotal,
                () -> "Lost updates detected. expectedTotal=" + expectedTotal +
                        ", actualTotal=" + actualTotal + ", mapSize=" + freq.size());
    }

    @RepeatedTest(500)
    void chm_merge_parallel_is_correct() {
        int[] nums = makeLotsOfNumbers(200_000, 100); // 200k items, values 0..99

        Map<Integer, Integer> freq = new ConcurrentHashMap<>();

        // ✔️ CHM + merge
        Arrays.stream(nums)
                .parallel()
                .boxed()
//                .forEach(n -> freq.put(n, freq.getOrDefault(n, 0) + 1));
                .forEach(n -> freq.merge(n, 1, Integer::sum));

        int expectedTotal = nums.length;
        int actualTotal = freq.values().stream().mapToInt(Integer::intValue).sum();

        // If no race happened this time, totals match.
        // If race happened, actualTotal is often LESS than expectedTotal due to lost updates.
        assertEquals(expectedTotal, actualTotal,
                () -> "Lost updates detected. expectedTotal=" + expectedTotal +
                        ", actualTotal=" + actualTotal + ", mapSize=" + freq.size());
    }

    @RepeatedTest(500) // run multiple times to increase chance of catching the race
    void groupingByConcurrent_parallel_is_correct() {
        int[] nums = makeLotsOfNumbers(200_000, 100); // 200k items, values 0..99

        Map<Integer, Long> freq = Arrays.stream(nums)
                .parallel()
                .boxed()
                .collect(Collectors.groupingByConcurrent(
                        Function.identity(),
                        Collectors.counting()
                ));

        long expectedTotal = nums.length;
        long actualTotal = freq.values().stream().mapToLong(Long::longValue).sum();

        assertEquals(expectedTotal, actualTotal);
    }

    private static int[] makeLotsOfNumbers(int size, int distinct) {
        int[] nums = new int[size];
        ThreadLocalRandom r = ThreadLocalRandom.current();
        for (int i = 0; i < size; i++) {
            nums[i] = r.nextInt(distinct);
        }
        return nums;
    }
}