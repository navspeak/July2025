package com.example.algo.linkedList;

public class ValidPalindrome {
    //https://leetcode.com/problems/palindrome-linked-list/description/
    class Solution {
        public boolean isPalindrome(ListNode head) {
            if (head == null || head.next == null) return true;

            // 1) Find middle (slow ends at middle)
            ListNode slow = head, fast = head;
            while (fast != null && fast.next != null) {
                slow = slow.next;
                fast = fast.next.next;
            }

            // If odd length, skip the middle node
            if (fast != null) { // fast not null => odd number of nodes
                slow = slow.next;
            }

            // 2) Reverse second half
            ListNode second = reverse(slow);

            // 3) Compare first half and reversed second half
            ListNode first = head;
            while (second != null) { // second half is shorter or equal
                if (first.val != second.val) return false;
                first = first.next;
                second = second.next;
            }

            return true;
        }

        private ListNode reverse(ListNode node) {
            ListNode prev = null, curr = node;
            while (curr != null) {
                ListNode next = curr.next;
                curr.next = prev;
                prev = curr;
                curr = next;
            }
            return prev;
        }
    }

// Typical ListNode definition:
// class ListNode { int val; ListNode next; ListNode(int v){val=v;} }
}
