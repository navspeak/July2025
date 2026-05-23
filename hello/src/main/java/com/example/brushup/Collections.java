package com.example.brushup;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

public class Collections {
    public static void main(String[] args) {
        CopyOnWriteArrayList<Integer> l =  new CopyOnWriteArrayList<>();
        l.add(10);
        l.add(11);
        l.add(12);
        l.add(0);
        System.out.println(l.remove(0)); //print 0
        System.out.println(Arrays.toString(l.toArray()));
        System.out.println(l.remove(Integer.valueOf(0))); // prints true
        System.out.println(Arrays.toString(l.toArray()));

//        PriorityQueue<Person> queue = new PriorityQueue<>((p1, p2) -> Integer.compare(p2.getAge(), p1.getAge()));
        PriorityQueue<Person> queue = new PriorityQueue<>(Comparator.comparingInt(Person::getAge).reversed());
        queue.add(new Person("Nav", 45));
        queue.add(new Person("Foo", 15));
        queue.add(new Person("Bar", 25));
        queue.add(new Person("Zar", 5));

        while(!queue.isEmpty()){
            System.out.println(queue.poll());
        }

        Deque<Integer> d = new ArrayDeque<>();
        d.add(1);
        d.add(2);
        d.add(3);
        d.addFirst(0);
        d.addLast(4);
        d.addLast(5);
        d.remove(); // Removes from Head
        d.removeFirst(); // Removes from Head
        d.pop(); // Removes from Head
        d.poll(); // Removes from Head
        d.push(3); // inserts at head
        System.out.println("-------");
        d.forEach(System.out::println); // 3,4,5
        d.push(99);
        System.out.println(d.pop());
//        NOTE: Deque doesn't support insert or get at an index

        // as queue => offer/poll() and stack -> push/pop
        Deque<String> queue1 = new ArrayDeque<>();

        // Enqueue
        queue1.offer("A");  // adds to tail
        queue1.offer("B");
        queue1.offer("C");

        System.out.println("Queue: " + queue);

        // Dequeue
        System.out.println("Dequeued: " + queue.poll());  // removes from head
        System.out.println("After dequeue: " + queue);

        // Peek at front
        System.out.println("Front element: " + queue.peek());

    }

    @Data
    @AllArgsConstructor
    private static class Person {
        String name;
        int age;
    }
}
