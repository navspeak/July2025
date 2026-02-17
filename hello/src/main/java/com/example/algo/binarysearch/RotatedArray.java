package com.example.algo.binarysearch;

public class RotatedArray {
    /*
https://leetcode.com/problems/find-minimum-in-rotated-sorted-array/description/
Example 1:
Input: nums = [3,4,5,1,2]
Output: 1
Explanation: The original array was [1,2,3,4,5] rotated 3 times.
Example 2:

Input: nums = [4,5,6,7,0,1,2]
Output: 0
Explanation: The original array was [0,1,2,4,5,6,7] and it was rotated 4 times.
Example 3:

Input: nums = [11,13,15,17]
Output: 11
Explanation: The original array was [11,13,15,17] and it was rotated 4 times.
     */
    public int findMin(int[] nums) {
        int l = 0, r = nums.length -1;
        while (l < r){
            int m = l + (r-l)/2;
            if (nums[m] > nums[r]) {
                l = m+1;
            } else {
                r = m;
            }
        }
        return nums[l];
    }

//    https://leetcode.com/problems/search-in-rotated-sorted-array/submissions/1917431482/

    public int search(int[] nums, int target) {
        int l = 0, r = nums.length -1;
        while (l < r){
            int m = l + (r-l)/2;
            if (nums[m] > nums[r]) {
                l = m+1;
            } else {
                r = m;
            }
        }

        int minIndex = l;
        if (target > nums[nums.length-1]){
            l =0;
            r = (minIndex -1) < 0 ? nums.length -1 : minIndex -1;
        } else {
            l = minIndex;
            r = nums.length -1;
        }
        while ( l <= r){
            int m = l + (r-l)/2;
            if (nums[m] == target) return m;
            if (target > nums[m]) l = m+1;
            else r = m- 1;
        }
        return -1;
    }
}
