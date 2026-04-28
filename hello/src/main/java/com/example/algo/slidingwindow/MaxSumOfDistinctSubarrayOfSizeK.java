package com.example.algo.slidingwindow;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class MaxSumOfDistinctSubarrayOfSizeK {

    /*
    Why this version is slightly safer:
    Clarity: Using right - left + 1 to check the distance is a common pattern that makes it clear
             you are measuring the physical window size.
    Order of Operations: Adding the element first and then checking if the window is too large is the
              standard "Expand then Contract" template for sliding windows.
     */
    //https://leetcode.com/problems/maximum-sum-of-distinct-subarrays-with-length-k/
    public static long maximumSubarraySum(int[] nums, int k) {
       Set<Integer> window = new HashSet<>(); // using boolean array of size 10^5 will improve perf due to boxing overhead wth set
       int left = 0;
       long sum = 0;
       long max = 0;
       for(int right = 0; right < nums.length; right++){
           int curr = nums[right];
           while(window.contains(curr)){
               sum-=nums[left];
               window.remove(nums[left]);
               left++;
           }

           sum +=curr;
           window.add(curr);

           if (right -left + 1 > k){
               sum -= nums[left];
               window.remove(nums[left]);
               left++;
           }

           if (right -left + 1 == k){
               max = Math.max(sum, max);
           }
       }
       return max;
    }
    
    public static long maximumSubarraySum_try1_correct_but_klunky(int[] nums, int k) {
        if (k <= 0 || k > nums.length) return 0;
        Set<Integer> window = new HashSet<>();
        long maxSum = 0;
        long sum = 0;
        for (int i = 0; i < nums.length; i++) {
            while(window.contains(nums[i])){
                int oldestInTheWindow = nums[i - window.size()];
                sum -= oldestInTheWindow;
                window.remove(oldestInTheWindow);
           }
            if (window.size() == k){
                sum-= nums[i - k];
                window.remove(nums[i - k]);
            }
            window.add(nums[i]);
            sum +=nums[i];
            if (window.size() == k) maxSum = Math.max(maxSum, sum);
        }
        return maxSum;
    }

    public static void main(String[] args) {
        long l = maximumSubarraySum(new int[]{1, 5, 4, 2, 9, 9, 9}, 3);
        System.out.println("l = " + l);

        l = maximumSubarraySum(new int[]{4, 4, 4, 4}, 3);
        System.out.println("l = " + l);

        l = maximumSubarraySum(new int[]{1, 2, 1, 1, 3, 4}, 3);
        System.out.println("l = " + l);
    }
}
