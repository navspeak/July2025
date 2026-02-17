package com.example.algo.twopointers;

public class ContainerWithMostWater {
    //    https://leetcode.com/problems/container-with-most-water/submissions/1922059222/
    public int maxArea(int[] height) {
        int l = 0, r = height.length - 1;
        int maxArea = 0;
        while (l < r){
            int currentArea = (r-l)*Math.min(height[l], height[r]);
            maxArea = Math.max(maxArea,  currentArea);
            if (height[l] < height[r]){
                l++;
            } else if (height[l] > height[r]){
                r--;
            } else {
                l++; r--;
            }

        }
        return maxArea;

    }
}
