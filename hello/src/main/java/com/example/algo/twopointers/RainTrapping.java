package com.example.algo.twopointers;

public class RainTrapping {
    public int trap(int[] height) {
        int l = 0, r = height.length -1;
        int maxL = 0, maxR = 0;
        int totalUnits = 0;
        /*
           totalUnit at i = Min(maxL, MaxR) - height[i]
           However, if height[L]<height[R], Min(maxL, MaxR) = maxL as we know maxR is > maxL
           similarly if height[L]<height[R], Min(maxL, MaxR) = maxR as we know maxL is > maxR
         */
        while(l<r){
            if (height[l] < height[r]){
                totalUnits += Math.max(maxL - height[l], 0);
                maxL = Math.max(maxL, height[l]);
                l++;
            } else {
                totalUnits += Math.max(maxR - height[r], 0);
                maxR = Math.max(maxR, height[r]);
                r--;
            }
        }
        return totalUnits;

    }
}
