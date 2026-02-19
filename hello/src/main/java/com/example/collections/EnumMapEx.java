package com.example.collections;

import java.util.EnumMap;

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


    }
}
