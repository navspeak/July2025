package com.example.collections;

import java.util.Map;

public class HashMap {
    public static void main(String[] args) {

        Map<String, String> map_immutable = Map.of("Name", "Navneet", "Address", "1122 some Road"); // Immutable Map
        Map<String, String> map = new java.util.HashMap<>(Map.of("Name", "Navneet", "Address", "1122 some Road")); // Immutable Map
        System.out.println(map.get("City")); // null as no key
        map.put("City", null);
        System.out.println(map.get("City")); // null as no key

    }
}
