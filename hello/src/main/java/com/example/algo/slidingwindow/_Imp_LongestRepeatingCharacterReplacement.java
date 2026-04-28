package com.example.algo.slidingwindow;

import java.util.HashMap;
import java.util.Map;

//https://leetcode.com/problems/longest-repeating-character-replacement/description/
public class _Imp_LongestRepeatingCharacterReplacement {

    public static int characterReplacement_Latest(String s, int k){
        Map<Character, Integer> window = new HashMap<>();
        int left = 0, right = 0;
        int max = 0, maxFreq = 0;
        while(right < s.length()){
            int freqOfCurrent = window.merge(s.charAt(right), 1, Integer::sum);
            maxFreq = Math.max(maxFreq, freqOfCurrent);
// maxSize cannot grow beyond maxFreq + k unless we later discover
// a larger real repeated count later in the scan.
//
// Example:
// s = "AABACAAAA", k = 1
//
// At window "AABA":
//   maxFreq = 3 ('A')
//   so largest possible valid size so far = 3 + 1 = 4
//
// Later, when more 'A' appear:
//   maxFreq becomes 4, then 5
//   so maxSize can grow to 5, then 6
// If new/different characters keep appearing,
//    max size will not increase unless one of them repeats enough to
//    raise maxFreq above 3.
// This is why we keep scanning instead of returning early.
            if(right - left +1 - maxFreq > k){
                window.compute(s.charAt(left), (key,v) -> {
                    if (v == 1) return null;
                    return v-1;
                });
                left++;
            }
            max = Math.max(max, right-left+1);
            right++;
        }
        return max;

    }

    private static boolean windowValid(Map<Character, Integer> window, int left, int right, int k) {
        var totalWindowSize = right - left;
        int maxVal = 0;
        // optimization: we can use maxVal calculated before shriniking as:
        // totalWindowSize - maxVal <= k; say maxVal = 5 but before shrinking was 7. totalWindowSize = 10, k = 5
        // if 10 - 5 <= 5 is true then 10 - 7 is also <=5
        // now say maxVal = 4 but before shrinking was 5. totalWindowSize = 10, k = 5
        // similarly, if 10 - 4 > 5 then 10 -5 is equal to 5. Meaning we can shrink 1, so not use while but if
        for (var value: window.values()){
            maxVal = Math.max(maxVal, value);
        }

        return totalWindowSize - maxVal <= k;
    }
    // ABBAACAAAAA 1
    // maxKey = 0, maxOccurence = 1, max = 2
    // L :0, R:0 -> {A:1}
    // L :0, R:1 -> {A:1, B:1} max = 2
    // L :0, R:1 -> {A:1, B:1} max = 2
    //
    public static int characterReplacement_array(String s, int k) {
        int chars[] = new int[26];//upper case alphabets
        int l =0, maxOccurence =0, max = 0;
        for (int r = 0; r < s.length(); r++) {
            maxOccurence = Math.max(maxOccurence, ++chars[s.charAt(r) - 'A']);
            int winLen = r - l + 1;
            if(winLen -maxOccurence >k){
                chars[s.charAt(l)- 'A']--;
                l++;
            }
            winLen = r - l + 1;
            max = Math.max(max, winLen);
        }
        return max;
    }


    public static int characterReplacement(String s, int k) {
        Map<Character, Integer> window = new HashMap<>();
        int left = 0;
//        int maxKey = -1;
        int maxOccurence = 0;
        int max = 0;
        for (int right = 0; right < s.length(); right++){
            char c = s.charAt(right);
            //add to window
            int newCount = window.merge(c, 1, Integer::sum);
            if (newCount > maxOccurence){
//                maxKey = s.charAt(right);
                maxOccurence = newCount;
            }
            //make the window valid
            if(right - left + 1-maxOccurence > k){ // we just do a if not while -> so no headache of maxOccurence
                char keyToDecrement = s.charAt(left);
                window.computeIfPresent(keyToDecrement, (key, value) -> {
                    if (value == 0) return null;
                    return value -1;
                });
                // NOTE: we accept stale max in the window - because ?
//                if (maxKey == keyToDecrement) maxOccurence--;
                left++;
            }

            max = Math.max(right - left + 1, max);
        }
        return max;
    }

    public static void main(String[] args) {
//        Map<Character, Integer> window = new HashMap<>();
//        System.out.println(window.merge('A', 1, Integer::sum));
//        System.out.println(window.merge('B', 1, Integer::sum));
//        System.out.println(window.merge('C', 1, Integer::sum));
//        System.out.println(window.merge('A', 1, Integer::sum));
//        System.out.println(window.merge('A', 1, Integer::sum));
        System.out.println(characterReplacement("ABAB", 0));
    }
}
/*
// Example:
// s = "AABABBA", k = 1
//
// Step when right = 4 (window = "AABAB"):
//   counts: A=3, B=2 → maxOccurence = 3
//   window size = 5 → 5 - 3 = 2 > 1 → we shrink
//
// After removing one 'A' from the left:
//   window becomes "ABAB"
//   counts: A=2, B=2 (true max freq is 2)
//   BUT maxOccurence is still 3 (stale!)
//
// Now check validity using stale max:
//   window size = 4 → 4 - 3 = 1 <= k  → we treat as valid
//
// In reality the window is slightly invalid (4 - 2 = 2 > 1),
// but this is fine because we already saw a valid window of size 4 earlier ("AABA").
//
// So stale maxOccurence may allow slightly larger windows temporarily,
// but it never causes us to record an incorrect maximum.
 */