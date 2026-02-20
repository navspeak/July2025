package com.example.algo.binarysearch;

import java.util.Arrays;

public class RangeOfAKey {
    public static int[] searchRange(int[] nums, int target) {
        if (nums == null || nums.length == 0) return new int[]{-1,-1};
        int l = 0, r = nums.length;
        while(l < r){
            int m = l + (r-l)/2;
            if (nums[m] >= target){
                r = m;
            } else {
                l = m+1;
            }
        }
        if (l >= nums.length || nums[l] != target )return new int[]{-1,-1};
        int start = l, end = l;
        while(start > 0 && nums[start] == nums[start -1]) start--;
        while(end < nums.length -1  && nums[end] == nums[end +1]) end++;
        return new int[]{start, end};
    }

    public static void main(String[] args) {
        System.out.println(Arrays.toString(searchRange(new int[]{5, 7, 7, 8, 8, 10}, 8)));
        System.out.println(Arrays.toString(searchRange(new int[]{5, 7, 7, 8, 8, 10}, 7)));
        System.out.println(Arrays.toString(searchRange(new int[]{5, 7, 7, 8, 8, 10}, 9)));
        System.out.println(Arrays.toString(searchRange(new int[]{5, 7, 7, 8, 8, 10}, 9)));
        System.out.println(Arrays.toString(searchRange(new int[]{}, 5)));
        System.out.println(Arrays.toString(searchRange(new int[]{2,2}, 5)));
    }
}
