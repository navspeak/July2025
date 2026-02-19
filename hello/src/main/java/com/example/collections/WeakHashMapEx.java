package com.example.collections;

import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

public class WeakHashMapEx {
    public static void main(String[] args) {
        record Person(String name, int age){}
        Person p1 = new Person("John", 23);
        Person p2 = new Person("Doe", 26);

        Map<Person, Integer> map = new HashMap<>();
        map.put(p1, 10);

        Map<Person, Integer> weakMap = new WeakHashMap<>();
        map.put(p1, 30);

        p1 = null;

        System.gc();
        System.out.println(map.size()); // 1
        System.out.println(weakMap.size()); // 0
    }
}
