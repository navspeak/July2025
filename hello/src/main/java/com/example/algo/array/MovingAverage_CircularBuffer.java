package com.example.algo.array;

public class MovingAverage_CircularBuffer {

    private int[] window;
    private int writePos;
    private int count;
    private int winSize;
    private double sum;

    public MovingAverage_CircularBuffer(int size) {
        window = new int[size];
        writePos = count= 0;
        sum = 0;
        winSize = size;
    }

    public double next(int val) {
       /*
            next(x) => [x,_,_] => writePos = 0-> 1, count = 0->1, sum = 0 + x, ret = sum/ 1
            next(y) => [x,y,_] => writePos = 1->2, count = 1->2, sum = 0 + x + y, ret = sum/2
            next(z) => [x,y,z] => writePos = 2-> 3 (wraps as 0), count = 2->3, sum = 0 + x + y + z, ret = sum/3
            Now count = 3, so we will need to remove from current position
            next(x1) => [(overwrite)x1,y,z] => writePos = 0->1, count = 3 (capped, sum = 0 + x + y + z -x+ x1, ret = sum/3
        */
        if (count == winSize){
            sum -=window[writePos];
        } else {
            count++;
        }

        window[writePos] = val;
        sum +=val;
        writePos = (writePos + 1) % winSize;
        return sum / count;
    }

    public static void main(String[] args) {
        /*
        Input
["MovingAverage", "next", "next", "next", "next"]
[[3], [1], [10], [3], [5]]
Output
[null, 1.0, 5.5, 4.66667, 6.0]
         */
        MovingAverage_CircularBuffer movingAverage = new MovingAverage_CircularBuffer(3);
        System.out.println(movingAverage.next(1));
        System.out.println(movingAverage.next(1));
        System.out.println(movingAverage.next(1));
        System.out.println(movingAverage.next(1));
        System.out.println(movingAverage.next(1));


    }
}
