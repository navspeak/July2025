package com.example.algo.twopointers;

public class SortColors {
    public void sortColors(int[] nums) {
        int start = 0;
        int mid = 0;
        int end = nums.length - 1;
        while (mid <= end){ // test case with 2 in front of array [2,0,1] or just [2,0]
            if (nums[mid] == 0){
                int tmp = nums[start];
                nums[start] = nums[mid];
                nums[mid] = tmp;
                start++;
                mid++;
            } else if (nums[mid] == 2){
                int tmp = nums[end];
                nums[end] = nums[mid];
                nums[mid] = tmp;
                end--; // Why only end
            } else{
                mid = mid + 1;
            }
        }
    }
}
