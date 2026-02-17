package com.example.algo.strings;

public class ValidPalindrome {
    public boolean validPalindrome(String s) {
        int i = 0, j = s.length() -1;
        while(i<j){
            if (s.charAt(i) == s.charAt(j)){
                i++; j--;
            } else {
                return ispal(s, i+1, j) || ispal(s, i, j-1);
            }
        }
        return true;
    }

    private boolean ispal(String s, int i, int j) {

        while(i<j){
            if (s.charAt(i) == s.charAt(j)){
                i++; j--;
            } else {
                return false;
            }
        }
        return true;
    }

    // ignore non alphanumeric
    public boolean isPalindrome(String s) {
        int l = 0, r = s.length() - 1;

        while (l < r) {

            // skip non-alphanumeric
            while (l < r && !Character.isLetterOrDigit(s.charAt(l))) {
                l++;
            }
            while (l < r && !Character.isLetterOrDigit(s.charAt(r))) {
                r--;
            }

            if (Character.toLowerCase(s.charAt(l)) !=
                    Character.toLowerCase(s.charAt(r))) {
                return false;
            }

            l++;
            r--;
        }

        return true;
    }

}
