package com.example.collections;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

public class FailFastSafe {

    public static void main(String[] args) {
        //ArrayList tracks a modCount => Iterator caches this as expectedModCount

        List<String> list = new ArrayList<>(List.of("A", "B", "C", "D"));

        //  Second-to-Last Element Trap.
        // The "for-each" loop internally creates an Iterator.
        // It tracks a 'cursor' (starts at 0) and 'expectedModCount'.
        for (String s : list) {

            // Iteration 1: cursor is 0. next() returns "A", cursor becomes 1.
            // Iteration 2: cursor is 1. next() returns "B", cursor becomes 2.
            // Iteration 3: cursor is 2. next() returns "C", cursor becomes 3.

            if (s.equals("C")) {
                // 1. Structural modification: "C" is removed.
                // 2. list.size() drops from 4 to 3.
                // 3. list.modCount increments (e.g., from 1 to 2).
                list.remove(s);

                // At this point: cursor = 3, list.size() = 3.
            }

            /* * THE TRAP:
             * After removing "C", the loop goes back to the top to check hasNext().
             * Iterator.hasNext() implementation: return cursor != size;
             * * Because 3 != 3 is FALSE, the iterator thinks it has reached the
             * end of the list and exits the loop gracefully.
             * * It NEVER calls next() again. Since the check for CME (checkForComodification)
             * only happens inside next(), the exception is never triggered.
             */
        }

        // Result: The loop finishes "successfully" but misses the last element "D".
        System.out.println(list); // Output: [A, B, D]

        // enhanced for each can be written as:
        for (Iterator<String> it = list.iterator(); it.hasNext(); ) {
            String s = it.next();              // ✅ check happens here
            if (s.equals("B")) list.remove(s); // ❌ modifies list outside iterator
            if (s.equals("B")) it.remove(); // ✅ use this

        }
        list.removeIf(s -> s.equals("B"));
    }


}

