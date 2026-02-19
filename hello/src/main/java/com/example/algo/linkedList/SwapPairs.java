package com.example.algo.linkedList;

public class SwapPairs {
    public ListNode swapPairs(ListNode head) {
        ListNode dummy = new ListNode(0, head);
        ListNode prev = dummy;
        ListNode ptr = dummy.next;
        while(ptr !=null && ptr.next !=null){
            ListNode curr = ptr;
            ListNode nextPair = curr.next.next;
            curr.next.next= curr;
            prev.next = curr.next;
            prev = curr;
            curr.next = nextPair;
            ptr = nextPair;

        }
        return dummy.next;
    }

    public ListNode reverseList(ListNode head) {
        ListNode prev = null;
        ListNode ptr =head;
        while(ptr !=null){
            ListNode next = ptr.next;
            ptr.next = prev;
            prev = ptr;
            ptr = next;
        }
        return prev;
    }
}
/*
null  dummy = 0->null, prev = 0->null, ptr = null
1      dummy = 0->1->null, prev = 0->1->null, ptr = 1->null

    0--------------> 1--->2---->3---->4
   (D)             (ptr)
   (prev)           curr   next   nextBlock

                      <--next
    0                1   2     3---->4
   (D)             (ptr)--^-----^
                    curr  |    nextBlock
     |--------------------|
                     (prev)
    D-->2--1-->3-->4

*/