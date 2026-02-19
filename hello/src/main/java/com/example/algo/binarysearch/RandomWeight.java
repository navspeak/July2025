package com.example.algo.binarysearch;

import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

//https://leetcode.com/problems/random-pick-with-weight/
public class RandomWeight {

    int weights[];
    int prefixSum[];
    int size;
    Random random = new Random();

    public RandomWeight(int[] w) {
        this.weights = new int[w.length];
        int sum = 0;
        for(int i = 0; i < w.length; i++){
            this.weights[i] = w[i];
            this.prefixSum[i] = sum + w[i];
            sum = this.prefixSum[i];
        }
        this.size = w.length;

    }
/*
weights = [1,2,5]
prefix  = [1,3,8]

Buckets (1..totalSum):
[1]   [2,3]   [4,5,6,7,8]
 0      1          2

Pick randomVal in [1..8]
Return the smallest index i such that randomVal <= prefix[i]

 */
    public int pickIndex() {
        int randomVal = random.nextInt(1, prefixSum[size - 1] + 1);
        int l = 0, r = size -1;
        //Find first Index where randomVal <= prefixSum at that index
        // lower_bound behavior
        while(l < r){
            int m = l + (r-l)/2;
            if (randomVal <= prefixSum[m]){
                r = m; // smallest index is at m or left of it
            } else {
                l = m +1; // smallest index is at right
            }
        }
        return l;
    }

    public int pickIndex_inbuilt() {
        // pick random in [1..total]
        int target =random.nextInt(1, prefixSum[size - 1] + 1);

        int idx = Arrays.binarySearch(prefixSum, target);

        // if not found, convert to insertion point
        if (idx < 0) {
            idx = -idx - 1;
        }

        return idx;
    }
    public static void main(String[] args) {
        int x = ThreadLocalRandom.current().nextInt(1, 6);
        int y = new Random().nextInt(1,6);
        int[] a = {1, 2, 2, 5, 6};
        int pos = Arrays.binarySearch(a, 4); // -4 -> insertion position
        System.out.println(pos);
        if (pos < 0){
            pos = -pos -1;
        }
        System.out.println(pos);
    }


}
