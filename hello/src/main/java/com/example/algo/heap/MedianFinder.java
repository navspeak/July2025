package com.example.algo.heap;

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
        /* why we add to left and then move to right
          To understand we have following invariants:
          1. All elements i left <= all elements in right
          2. Left has equal or more elements than right
          say: left [ 3 2 1] right [ 10, 20]
          if 100 came, we add to left [ 100, 3 2 1]
          Rebalance : move max of left -> right
         */

        if (left.size() < right.size()){
            left.add(right.poll());
        }
    }

    public void addNum_verbose(int num) {
        if (left.isEmpty() || num <= left.peek()) {
            left.add(num);
        } else {
            right.add(num);
        }

        // rebalance sizes
        if (left.size() > right.size() + 1) {
            right.add(left.poll());
        } else if (right.size() > left.size()) {
            left.add(right.poll());
        }
    }

    public double findMedian() {
        if (left.size() > right.size()) return left.peek();
        return (left.peek() + right.peek())/2.0;
    }
}

/*
Yep—let’s dry-run 1, 2, 3, 4
left (max-heap) = lower half (top is largest of lower half)
right (min-heap) = upper half (top is smallest of upper half)
===
Add 1

Step1: left.add(1) → left=[1] right=[]
Step2: right.add(left.poll()) → left=[] right=[1]
Step3: left<size? yes → left.add(right.poll()) → left=[1] right=[]

Median (odd): left.peek() = 1
====
Add 2

Start: left=[1], right=[]

Step1: left.add(2) → left=[2,1]
Step2: move max(left)=2 → right → left=[1], right=[2]
Step3: left<size? (1<1) no

Now: left=[1], right=[2] (sizes equal)

Median (even): (1 + 2)/2 = 1.5
===
Add 3

Start: left=[1], right=[2]

Step1: left.add(3) → left=[3,1]
Step2: move max(left)=3 → right → left=[1], right=[2,3]
Step3: left<size? (1<2) yes → move min(right)=2 → left
→ left=[2,1], right=[3]

Median (odd): left.peek() = 2
===
Add 4

Start: left=[2,1], right=[3]

Step1: left.add(4) → left=[4,1,2]
Step2: move max(left)=4 → right → left=[2,1], right=[3,4]
Step3: left<size? (2<2) no

Now: left=[2,1], right=[3,4] (sizes equal)

Median (even): (2 + 3)/2 = 2.5
 */