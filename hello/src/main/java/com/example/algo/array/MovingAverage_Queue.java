package com.example.algo.array;

import java.util.ArrayDeque;
import java.util.Queue;

public class MovingAverage_Queue {

    private Queue<Integer> queue;
    private int winSize;
    private double sum;

    public MovingAverage_Queue(int size) {
       queue = new ArrayDeque(size);
       winSize = size;
    }

    public double next(int val) {
       if (queue.size() == winSize){
           sum = sum - queue.remove();
       }
       queue.add(val);
       sum +=val;
//       return queue.stream().mapToDouble(Integer::intValue).sum() / queue.size();
       return sum / queue.size();
    }



    public static void main(String[] args) {
        /*
        Input
["MovingAverage", "next", "next", "next", "next"]
[[3], [1], [10], [3], [5]]
Output
[null, 1.0, 5.5, 4.66667, 6.0]
         */
        MovingAverage_Queue movingAverage = new MovingAverage_Queue(3);
        System.out.println(movingAverage.next(10));
        System.out.println(movingAverage.next(20));
        System.out.println(movingAverage.next(30));
        System.out.println(movingAverage.next(40));
        System.out.println(movingAverage.next(50));


    }
}
