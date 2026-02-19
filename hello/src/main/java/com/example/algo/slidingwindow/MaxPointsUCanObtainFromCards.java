package com.example.algo.slidingwindow;

public class MaxPointsUCanObtainFromCards {

    public int maxScore_sliding_window(int[] cardPoints, int k){
        /*
            [1,2,3,4,  5, 6,1]
            we need to maximize: [1,2,3] [] or [1,2] [1] or [1] [6 1] or [] [5 6 1]
             sum = 1+2+3
            count = 0 => remove cardPoints[0] = 1 and add cardPoints[n] = 1
            count = 1 => remove cardPoints[1] = 2 and add cardPoints[n-1] = 6
            ...

         */

        int sum = 0;
        for (int i = 0; i< k; i++) sum+=cardPoints[i];
        int max = sum;
        int i = 0;
        int n = cardPoints.length -1;
        while (i < k){ //0, 1, 2 k = 3
            /*
                Before Loop:  [1,2,3] []
                In Loop i = 0:  [1,2] [1] k-i -1= 2 & n-i = 6
                In Loop i = 1:  [1] [6 1] k-i -1= 3-1-1 = 1 & n-i = 6-1= 5
                In Loop i = 2:  [] [5 6 1] k-i -1= 3-2-1 = 0 & n-i = 6-2= 4
             */
            sum -=cardPoints[k - i -1];
            sum +=cardPoints[n - i];
            max = Math.max(sum, max);
            i++;
        }
        return max;
    }

    public int maxScore_prefix_sum_O_K(int[] cardPoints, int k) {
        int n = cardPoints.length;

        int[] left = new int[k + 1];   // left[i] = sum of first i
        int[] right = new int[k + 1];  // right[i] = sum of last i

        /*
           [1,2,3,4,5,6,1]
            0 3 5
                     11 7 0
         */
        for (int i = 1; i <= k; i++) {
            left[i] = left[i - 1] + cardPoints[i - 1];
            right[i] = right[i - 1] + cardPoints[n - i];
        }

        int max = 0;
        for (int i = 0; i <= k; i++) {
            max = Math.max(max, left[i] + right[k - i]);
        }
        return max;
    }

    public int maxScore_brute_O_K_squared(int[] cardPoints, int k) {
       /*
        [1,2,3,4,5,6,1]
        Take 0 from Left + k from Right
        Take 1 from Left + k-1 from Right
        ..
        Take k from Left + 0 from Right

        [1,2,3,4,5,6,1] 3

        [] [561]
        [1] [61] i=1, c=0,1
        [12] [1] i=2, c=0,1


*/

        int max = 0;
        for (int i = 0; i <= k; i++){
            int leftSum = 0;
            int count = 0;
            while (count < i){
                leftSum +=cardPoints[count++];
            }
            count = 0;
            int rightSum = 0;
            while (count < k-i){
                rightSum +=cardPoints[cardPoints.length-1 - count];
                count++;
            }
            max = Math.max(max, leftSum+rightSum);
        }
        return max;
    }

}
