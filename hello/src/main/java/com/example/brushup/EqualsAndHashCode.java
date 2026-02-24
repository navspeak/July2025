package com.example.brushup;

import java.util.HashMap;

public class EqualsAndHashCode {
    public static void main(String[] args) {

        HashMap<Person, String> map = new HashMap<>();

        Person p1 = new Person("Alice", 30);
        Person p2 = new Person("Bob", 25);
        Person p3 = new Person("Alice", 30);  // same data as p1

        map.put(p1, "Engineer");
        map.put(p2, "Doctor");

        // question 1: I expect output to be Engineer as Alice Engineer. Will it be so, if not why and how to fix
        System.out.println("Job of p3: " + map.get(p3));

        // Check if map contains p3 as key
        System.out.println("Contains p3 key? " + map.containsKey(p3)); // true
    }

    @lombok.EqualsAndHashCode
    public static class Person {
        String name;
        int age;

        public Person(String name, int age) {
            this.name = name;
            this.age = age;
        }
    }
}