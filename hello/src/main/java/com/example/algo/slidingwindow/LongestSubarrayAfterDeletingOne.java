package com.example.algo.slidingwindow;
//https://leetcode.com/problems/longest-subarray-of-1s-after-deleting-one-element/
public class LongestSubarrayAfterDeletingOne {
    public static int longestSubarray(int[] nums) {
        int zeros =0, ones = 0;
        int max = 0;
        int left = 0;
        for (int right = 0; right < nums.length; right++){
            if (nums[right] == 0) zeros++;
            else ones++;

            while (zeros > 1){
                if (nums[left] == 0) zeros--;
                else ones--;
                left++;
            }
            max = Math.max(right - left, max);
        }
        return max;
    }

    public static void main(String[] args) {
        var res = longestSubarray(new int[]{0,1,1,1,1,1,0,1});
        System.out.println("res = " + res);
    }
}
/*
Given a binary array nums, you should delete one element from it.

Return the size of the longest non-empty subarray containing only 1's in the resulting array. Return 0 if there is no such subarray.



Example 1:

Input: nums = [1,1,0,1]
Output: 3
+Example 2:
















Input: nums = [0,1,1,1,0,1,1,0,1]
Output: 5
Explanation: After deleting the number in position 4, [0,1,1,1,1,1,0,1] longest subarray with value of 1's is [1,1,1,1,1].
Example 3:

Input: nums = [1,1,1]
Output: 2
Explanation: You must delete one element.
 */