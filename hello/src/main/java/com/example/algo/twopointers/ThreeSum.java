package com.example.algo.twopointers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ThreeSum {
    public List<List<Integer>> threeSum(int[] nums) {
        Arrays.sort(nums);
        List<List<Integer>> result = new ArrayList<>();
        for (int i = 0; i< nums.length -2; i++){
            if (i > 0 && nums[i] == nums[i-1]) continue;
            int curr = nums[i];
            int rem = - curr;
            int l = i+1, r = nums.length-1;
            while (l < r){
                if (nums[l] + nums[r] == rem ){
                    result.add(List.of(curr, nums[l], nums[r]));
                    l++; r--;
                    while (l < r && nums[l] == nums[l-1]) l++;
                    while (l < r && nums[r] == nums[r+1]) r--;
                } else if (nums[l] + nums[r] < rem){
                    l++;
                } else {
                    r--;
                }
            }

        }
        return result;
        }

    public static void main(String[] args) {
        int [] nums = new int[]{4,1,2,3};
        Arrays.sort(nums);
        System.out.println(Arrays.toString(nums));
    }

}
