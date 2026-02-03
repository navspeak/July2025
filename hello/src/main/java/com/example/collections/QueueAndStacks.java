package com.example.collections;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;

public class QueueAndStacks {
    public static void main1(String[] args) {
//| Method     | Failure Behavior | Intended Use        |
//| ---------- | ---------------- | ------------------- |
//| `add()`    | Throws exception | Strict              |
//| `offer()`  | Returns `false`  | Safe / non-blocking |
//| `remove()` | Throws exception | Strict              |
//| `poll()`   | Returns `null`   | Safe / non-blocking |

//| Method     | On Failure             | Failure Condition |
//| ---------- | ---------------------- | ----------------- |
//| `add()`    | IllegalStateException  | full              |
//| `offer()`  | false                  | full              |
//| `remove()` | NoSuchElementException | empty             |
//| `poll()`   | null                   | empty             |
//
// | Java Queue Concept | Messaging Analogy |
// | ------------------ | ----------------- |
// | `offer()`          | try-produce       |
// | `poll()`           | try-consume       |
// | bounded queue      | backpressure      |
// | `false` on offer   | buffer full       |


    }

    static void boundedQ(){
        Queue<Integer> q = new ArrayBlockingQueue<>(2);
        // offer and add - appendleft or appendright?
        q.offer(1); // true
        q.offer(2); // true
        q.offer(3); // false (queue full)
        q.add(3); // throws IllegalStateException

    }


    static void unbounded(){
//        | Implementation          | offer() returns false     |
//        | ----------------------- | ------------------------- |
//        | `ArrayDeque`            | Never                     |
//        | `LinkedList`            | Never                     |
//        | `ConcurrentLinkedDeque` | Never                     |

//        Failure can occur only due to OutOfMemoryError, not API semantics.
    }
    static void queue(){
        /* When you use a Deque through the Queue interface, add / offer / remove / poll always operate on the tail for
        insert and head for removal.

Queue view (FIFO):
                     HEAD            TAIL
remove / poll <--- [ 1 ][ 2 ][ 3 ] <--- add / offer
                   (Front/Left)          (Back/Right)

| Call         | Equivalent Deque call | Python analogy |
| ------------ | --------------------- | -------------- |
| `q.add(e)`   | `dq.addLast(e)`       | `append(e)`    |
| `q.offer(e)` | `dq.offerLast(e)`     | `append(e)`    |

| Call         | Equivalent Deque call | Python analogy |
| ------------ | --------------------- | -------------- |
| `q.remove()` | `dq.removeFirst()`    | `popleft()`    |
| `q.poll()`   | `dq.pollFirst()`      | `popleft()`    |

         */
    }

    static void deque(){
//        [ FRONT / HEAD ]  ........  [ BACK / TAIL ]
//        First = Front = Head
//
//        Last = Back = Tail
//        Deque.addFirst == Python deque.appendleft
//        addFirst  = addLeft  = FRONT / HEAD = Python appendleft
//        addLast   = addRight = BACK  / TAIL = Python append
        Deque<Integer> stack = new ArrayDeque();

        stack.push(1);   // same as addFirst
        stack.push(2);
        stack.push(3);

        int x = stack.pop();  // same as removeFirst
    }

    
}
