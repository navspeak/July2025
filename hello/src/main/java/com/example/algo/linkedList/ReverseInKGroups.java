package com.example.algo.linkedList;
//https://leetcode.com/problems/reverse-nodes-in-k-group/description/
public class ReverseInKGroups {

    /*
        Dummy -> 1->2 ->3. k = 2
        prev       kth  groupNext

        reverse(prev, k)  (rev)2->1(curr)

     */
    public ListNode reverseKGroup(ListNode head, int k) {
        ListNode dummy = new ListNode(-1, head);
        ListNode prev = dummy;
        ListNode curr = dummy.next;
        while(true/*?*/){
            ListNode kth = getkth(prev, k);
            if (kth == null) break;
            ListNode nextGroup = kth.next;
            ListNode reversed = reverse(curr, nextGroup);
            prev.next = reversed;
            curr.next = nextGroup;
            prev = curr;
            curr = nextGroup;
        }
        return dummy.next;
    }

    private ListNode reverse(ListNode curr, ListNode nextGroup){
        ListNode prev = null;
        while(curr !=nextGroup){
            ListNode next = curr.next;
            curr.next = prev;
            prev = curr;
            curr = next;
        }
        return prev;
    }

    private ListNode getkth(ListNode head, int k){
        while(head !=null && k > 0){
            head = head.next;
            k--;
        }
        return head;
    }
}
