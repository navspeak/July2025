package com.example.collections;

import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;

public class IdentityHashMapEx {
    //👉 IdentityHashMap is a Map implementation that uses reference equality (==) instead of .equals() to compare keys.
    // it’s typically used in frameworks or object graph processing where object identity matters,
    // such as tracking visited nodes, handling circular dependencies, or caching proxies.
    public static void main(String[] args) {
        String a = new String("hello");
        String b = new String("hello");

        Map<String, Integer> map = new HashMap<>();
        map.put(a, 1);
        map.put(b, 2);

        System.out.println(map.size()); // 👉 1

        Map<String, Integer> map1 = new IdentityHashMap<>();
        map1.put(a, 1);
        map1.put(b, 2);

        System.out.println(map1.size()); // 👉 2 => because a != b although a.equals(b)

    }
}
