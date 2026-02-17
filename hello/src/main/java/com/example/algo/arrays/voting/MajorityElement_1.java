package com.example.algo.arrays.voting;

import java.util.*;

/*
Given an array nums of size n, return the majority element.

The majority element is the element that appears more than ⌊n / 2⌋ times. You may assume that the majority element always exists in the array.

Example 1:

Input: nums = [3,2,3]
Output: 3
Example 2:

Input: nums = [2,2,1,1,1,2,2]
Output: 2


Constraints:

n == nums.length
1 <= n <= 5 * 104
-109 <= nums[i] <= 109
The input is generated such that a majority element will exist in the array.


Follow-up: Could you solve the problem in linear time and in O(1) space?
 */
public class MajorityElement_1 {
    public static int majority(int[] nums){
        Map<Integer, Integer> freq = new HashMap<>();
        int max = 0;
        int maxKey = -1;
        for(int n : nums){
            freq.merge(n, 1, Integer::sum);
            if (freq.get(n) > max){
                max = freq.get(n);
                maxKey = n;
            }
        }

        return maxKey;
    }
    /*
## Key Insight
Majority element survives "cancellation" - if you pair each
majority element with a different element, majority still has
elements left over

## Algorithm: Boyer-Moore Voting
1. Maintain candidate + count
2. When count=0, update candidate
3. If current==candidate: count++, else count--
4. Candidate at end is majority (if exists)

## Why It Works
- Majority appears > n/2 times
- Every time we "cancel" majority with non-majority,
  we remove 2 elements
- Even in worst case, majority has leftovers
     */

    public static int majority_no_extra_space(int[] nums){
        /*      Boyer–Moore Voting Algorithm
                count = 0
                res = 1
                [1,2,2,1,1,2,2,2]

         */
        int count = 1, res = 0;
        for (int i = 0; i< nums.length; i++){
            if (nums[i] == res) {
                count++;
                continue;
            }
            count--;
            if (count < 0){
                res = nums[i];
                count=1;
            }
        }
        return res;
    }


}
