package com.example.algo.binarysearch;


public class PeakElement {
    public int findPeakElement(int[] nums) {
        int l = 0, r = nums.length -1;
        while (l < r){
            int mid = l + (r -l)/2;
            if (nums[mid] > nums[mid + 1]) r = mid;
            else l = mid +1;
        }
        return l;
    }


    public static void main(String[] args) {

    }
}
