package com.example.algo.arrays.voting;

import java.util.*;

public class MajorityElement_2 {
    //    https://leetcode.com/problems/majority-element-ii/description/
    public List<Integer> majorityElement(int[] nums) {

        Map<Integer, Integer> candidates = new HashMap<>(2);

        for(var n: nums){
            candidates.merge(n, 1, Integer::sum);
            if (candidates.size() > 2) {
                // Decrement all and remove zeros
                candidates.replaceAll((key, count) -> count - 1);
                candidates.values().removeIf(count -> count == 0);
            }
        }

        // Phase 2: Verify candidates
        int threshold = nums.length / 3;
        List<Integer> result = new ArrayList<>();

        for (int candidate : candidates.keySet()) {
            long count = Arrays.stream(nums)
                    .filter(n -> n == candidate)
                    .count();
            if (count > threshold) {
                result.add(candidate);
            }
        }
        return result;
    }

    //Recommended
    public List<Integer> majorityElement_2(int[] nums) {
        // Phase 1: Find up to 2 candidates
        Integer candidate1 = null, candidate2 = null;
        int count1 = 0, count2 = 0;

        for (int n : nums) {
            if (candidate1 != null && n == candidate1) {
                count1++;
            } else if (candidate2 != null && n == candidate2) {
                count2++;
            } else if (count1 == 0) {
                candidate1 = n;
                count1 = 1;
            } else if (count2 == 0) {
                candidate2 = n;
                count2 = 1;
            } else {
                count1--;
                count2--;
            }
        }

        // Phase 2: Verify candidates
        count1 = 0;
        count2 = 0;
        for (int n : nums) {
            if (candidate1 != null && n == candidate1) count1++;
            else if (candidate2 != null && n == candidate2) count2++;
        }

        List<Integer> result = new ArrayList<>();
        int threshold = nums.length / 3;
        if (count1 > threshold) result.add(candidate1);
        if (count2 > threshold) result.add(candidate2);

        return result;
    }



}
