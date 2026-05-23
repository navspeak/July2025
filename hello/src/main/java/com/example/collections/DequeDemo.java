package com.example.collections;

import java.util.ArrayDeque;
import java.util.Deque;

public class DequeDemo {

    public static void main(String[] args) {
        queueDemo();
        stackDemo();
    }

    /*

  - add = addLast = offer — all tail insertion, differ only on failure
  - remove = removeFirst = poll — all head removal, differ only on failure
  - addFirst = push — head insertion (stack behavior, opposite end)

 Head (index 0)          Tail (last)
       ↓                      ↓
    [ x,   y,   z ]
       ↑                      ↑
  poll/remove/pop          offer/add/addLast insert here
  addFirst/push insert here

     */

    static void queueDemo() {
        System.out.println("=== Queue (FIFO) ===");
        Deque<String> queue = new ArrayDeque<>();

        queue.offer("x");
        queue.offer("y");
        queue.offer("z");
        System.out.println("After offer x,y,z: " + queue);  // [x, y, z]

        System.out.println("poll: " + queue.poll());          // x
        System.out.println("poll: " + queue.poll());          // y
        System.out.println("poll: " + queue.poll());          // z
        System.out.println("poll empty: " + queue.poll());    // null
    }

    static void stackDemo() {
        System.out.println("\n=== Stack (LIFO) ===");
        Deque<String> stack = new ArrayDeque<>();

        stack.push("x");
        stack.push("y");
        stack.push("z");
        System.out.println("After push x,y,z: " + stack);   // [z, y, x]

        System.out.println("pop: " + stack.pop());            // z
        System.out.println("pop: " + stack.pop());            // y
        System.out.println("peek: " + stack.peek());          // x (not removed)
        System.out.println("pop: " + stack.pop());            // x
    }
}
