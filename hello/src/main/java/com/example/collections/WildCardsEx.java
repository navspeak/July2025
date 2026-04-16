package com.example.collections;

import java.util.ArrayList;
import java.util.List;

public class WildCardsEx {
    public static void main(String[] args) {
        sumList(new ArrayList<Integer>());  // ✅ works
        sumList(new ArrayList<Double>());  // ✅ works

        addNumbers(new ArrayList<>());  // ✅ works
        addNumbers(new ArrayList<Number>());   // ✅ works
        addNumbers(new ArrayList<Object>());   // ✅ works
    }

    public static double sumList(List<? extends Number> list) {
        double sum = 0;
        for (Number n : list) {  // ✅ Reading is fine
            sum += n.doubleValue();
        }
//        list.add(10); ❌ compiler doesnt know the exact type - Int, Double?
        return sum;
    }

    // You can add to this list, but reading gives you only Object
    public static void addNumbers(List<? super Integer> list) {
        list.add(1);    // ✅ Writing is fine
        list.add(2);    // ✅
        // Integer n = list.get(0);  ❌ can only get Object
    }
    /*
Classic Example — Collections.copy()
The JDK itself uses PECS perfectly:
public static <T> void copy(List<? super T> dest,   // Consumer — we write into dest
                             List<? extends T> src) { // Producer — we read from src
    for (T item : src) {
        dest.add(item);
    }
}
     */

}
