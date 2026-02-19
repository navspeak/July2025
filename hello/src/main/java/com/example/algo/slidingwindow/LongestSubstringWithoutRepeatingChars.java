package com.example.algo.slidingwindow;

import java.util.HashSet;
import java.util.Set;

public class LongestSubstringWithoutRepeatingChars {

    public static int lengthOfLongestSubstring(String s) {
        Set<Character> window = new HashSet<>();
        int left = 0;
        int max = 0;
        for (int right =0; right < s.length(); right++){
            char c =  s.charAt(right);
            while (window.contains(c)){
                window.remove(s.charAt(left));
                left++;
            }

            window.add(c);
            max = Math.max(max, right - left + 1);
        }
        return max;
    }

    public static void main(String[] args) {
        var res = lengthOfLongestSubstring(" ");
        System.out.println("res = " + res);
    }
}
