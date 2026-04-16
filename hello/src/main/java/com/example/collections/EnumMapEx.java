package com.example.collections;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class EnumMapEx {
    /*
    🔥 Why does EnumMap exist?
        Because enum keys are very predictable:
        1. finite set -> known at compile time -> have ordinal positions (0,1,2,…)
    👉 So Java uses an array-backed implementation instead of hashing.
        That makes it:
        ✔ faster than HashMap
        ✔ more memory efficient
        ✔ no hash collisions
     */

    public static void main(String[] args) {
        enum OrderStatus {
            NEW, PROCESSING, COMPLETED, FAILED
        }
        EnumMap<OrderStatus, String> statusMessages =
                new EnumMap<>(OrderStatus.class);

        statusMessages.put(OrderStatus.NEW, "Order created");
        statusMessages.put(OrderStatus.PROCESSING, "In progress");
        statusMessages.put(OrderStatus.COMPLETED, "Done");

        System.out.println(statusMessages.get(OrderStatus.NEW));
        Map<Person, Integer> map = new HashMap<>();
        var p1 = new Person(1, "jjj");
        var p2 = new Person(41, "jj888j");
        var p3 =new Person(491, "jj889998j");
        var p4 =new Person(4917, "jj8899rtur98j");
        var p5 =new Person(49175, "jj8899ryu98j");
        var p6 =new Person(4961, "jj88999ur8j");
        map.put(p1, 1);
        map.put(p1, 12);
        map.put(p1, 13);
        map.put(p2, 14);
        map.put(p3, 18);
        map.put(p4, 181);
        map.put(p5, 18);
        map.put(p6, 18);
        System.out.println(map.size());
        System.out.println(map.get(p1));
        p1.x = 2; //
        System.out.println(map.get(p1));
        System.out.println();

    }
    static class Person {
        int x;
        String Y;

        public Person(int x, String y) {
            this.x = x;
            Y = y;
        }

        @Override
        public int hashCode() {
            return x % 3;
        }
    }
}
