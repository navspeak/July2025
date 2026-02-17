package com.example.collections;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.IntStream;

public class HashMapEx {
    public static void main(String[] args) {
        NullKeyAllowedInHashMapNotInCHM();
        /* 1 */computeExample();
        /* 2 */computeIfAbsentExample();
        /* 3 */computeIfAbsentExample_IfKeyMissing_CreateList();
        /* 4 */mergeExample_SetIfPresent_ElseAddToExisting();
        /* 5 */replaceAndRemoveExample();

        /* 6 */
        put_containsKey_oldWay();
        put_getOrDefaults_notAtomic_slightlyFaster_InTightLoop();
        useMerge_atomic();
        /*
        Local loop, no threads? → getOrDefault
        ConcurrentHashMap / shared state? → merge
        Want expressive intent? → merge
         */

        Map<Integer, Integer> freq = new HashMap<>();
        int[] nums = {1,2,3,3,3,4};
        Arrays.stream(nums)
                .boxed()
                .forEach(n -> freq.put(n, freq.getOrDefault(n, 0) + 1));
        int keyWithmaxCount;
        int maxCount = 0;
        for (var item : freq.entrySet()){
            if (item.getValue() > maxCount) {
                maxCount = item.getValue();
                keyWithmaxCount = item.getKey();
            }

        }
    }

    private static void replaceAndRemoveExample() {
        int[] nums = {1,2,3,4,5};
        Map<Integer, Integer> candidates = new HashMap<>(2);
        /* add count to map
           If count > 2 decrement all by 1
           If any value become zero, remove
         */
        for(var n: nums){
            candidates.merge(n, 1, Integer::sum);
            if (candidates.size() > 2) {
                // Decrement all and remove zeros
                candidates.replaceAll((key, count) -> count - 1);
                candidates.values().removeIf(count -> count == 0);
            }
        }
    }

    private static void useMerge_atomic() {
        int[] nums = {1,2,3,3,3,4};
        Map<Integer, Integer> freq = new HashMap<>();
        for (int n : nums) {
//            freq.merge(n, 1, (old, newval) -> old + newval);
            freq.merge(n, 1, Integer::sum);
        }
    }

    private static void put_getOrDefaults_notAtomic_slightlyFaster_InTightLoop() {
        int[] nums = {1,2,3,3,3,4};
        Map<Integer, Integer> freq = new HashMap<>();
        for (int n : nums) {
            freq.put(n, freq.getOrDefault(n, 0) + 1);
        }
    }

    private static void put_containsKey_oldWay() {
        int[] nums = {1,2,3,3,3,4};
        Map<Integer, Integer> freq = new HashMap<>();
        for(int n : nums){
            if (freq.containsKey(n)){
                freq.put(n, freq.get(n)+1);
            } else {
                freq.put(n,1);
            }
        }
    }

    private static void NullKeyAllowedInHashMapNotInCHM() {
        //---------------------------------
        Map<String, String> map_immutable = Map.of("Name", "Navneet", "Address", "1122 some Road"); // Immutable Map
        Map<String, String> map = new HashMap<>(Map.of("Name", "Navneet", "Address", "1122 some Road")); // Immutable Map
        System.out.println(map.get("City")); // null as no key
        map.put("City", null);
        map.put(null, "Null");
        System.out.println(map.get("City")); // null as no value
        System.out.println("Null key" + map.get(null)); // null key

        Map<String, String> map2 = new ConcurrentHashMap<>(Map.of("Name", "Navneet", "Address", "1122 some Road")); // Immutable Map
        System.out.println(map2.get("City")); // null as no key
        try {
            map2.put("City", null); // will fail - CHM doesn't allow null
        } catch (NullPointerException e) {
            System.out.println("❌Putting null value in CHM is not allowed");
        }
        System.out.println(map2.get("City")); // null as no value
        try {
            map2.put(null, "Null");
            System.out.println("Null key ->" + map2.get(null)); // null key
        } catch (NullPointerException e) {
            System.out.println("❌Putting null key in CHM is not allowed");
        }
        //---------------------------------
    }

    private static void computeExample() {
        Map<String, Integer> counts = new HashMap<>();
        List<String> words = List.of("apple", "banana", "apple");

        for (String w : words) {
            // If v is null (absent), we start at 1. Otherwise, we add 1.
            counts.compute(w, 
                    (key, value) ->
                            (value == null) ? 1 : value + 1);
        }
    }

    private static void computeIfAbsentExample() {
        Map<String, Integer> counts = new HashMap<>();
        List<String> words = List.of("apple", "banana", "apple");
        counts.computeIfAbsent("count", x -> {
            System.out.println("Total no. of fruits " + words.size());
            return words.size();
        });
    }

    private static void computeIfAbsentExample_IfKeyMissing_CreateList() {
        Map<String, List<String>> groups = new HashMap<>();
        // computeIfAbsent handles the "if key missing, create list" logic
        groups.computeIfAbsent("fruits", k -> new ArrayList<>()).add("apple");
    }

    private static void mergeExample_SetIfPresent_ElseAddToExisting() {
        Map<String, Integer> counter = new HashMap<>();
        // If missing, set to 1. If present, add 1 to existing value (v).
        counter.merge("clicks", 1, (oldValue, newValue) -> oldValue + newValue);
    }



}
