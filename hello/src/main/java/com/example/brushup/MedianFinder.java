package com.example.brushup;

import java.util.Comparator;
import java.util.PriorityQueue;

class MedianFinder {

    PriorityQueue<Integer> left, right;
    public MedianFinder() {
        left = new PriorityQueue<>(Comparator.reverseOrder());
        right = new PriorityQueue<>();

    }

    public void addNum(int num) {
        left.add(num);
        right.add(left.poll());
        if (left.size() < right.size()){
            left.add(right.poll());
        }
    }

    public double findMedian() {
        if (left.size() > right.size()) return left.peek();
        return (left.peek() + right.peek())/2.0;
    }
}