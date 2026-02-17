package com.example.algo.twopointers;

import java.util.Arrays;

public class CountValidTriangles {
    public int triangleNumber(int[] nums) { //O(N^2)
        Arrays.sort(nums);
        int count = 0;
        for (int i = nums.length - 1; i>=2; i--){
            // with nums[i] let's check how many combinations are possible
            int l = 0, r = i -1;
            while(l < r){
                if (nums[l] + nums[r] > nums[i]){
                    // if true, all combinations from l = l+1 to r will be gt num[i]
                    count += r - l;
                    // not we have taken into consideration all combinations with element at so move to next
                    r--;
                } else {
                    // we need to move right to increase the sum
                    l++;
                }
            }
        }
        return count;
    }
}

/*
0 1 2 3
----------------------
2 3 4 4

i = 3
   l = 0, r = 2
    2+4 > 4 count = 2
    r =1
   l = 0, r = 1
     2+3 > 4 count = 2 + 1 = 3
     r = 0
i = 2
   l = 0, r = 1
    2+3 > 4 count = 2+1+1
    r =0
i = 1

*/
