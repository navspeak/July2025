package com.example.algo.linkedList;

import static com.example.algo.linkedList.ListNode.print;

public class RemoveNthFromLast {

    public static ListNode removeNthFromEnd(ListNode head, int n) {

        ListNode dummy = new ListNode(0, head);
        ListNode ahead = dummy;
        ListNode behind = dummy;
        // Create a gap of exactly n nodes between ahead and behind.
        // IMPORTANT
        for(int i = 0; i <= n; i++){
            if (ahead == null) {
                System.out.println("Wrong input");
                return head;
            }
            ahead = ahead.next;
        }

        while(ahead != null){
            behind = behind.next;
            ahead = ahead.next;
        }

        behind.next = behind.next.next;
        return dummy.next;
    }

    public static void main(String[] args) {
        ListNode head = ListNode.createList(1,2,3,4,5,6);
        print(head);
        print(removeNthFromEnd(head, 2));

        head = ListNode.createList(1);
        print(head);
        print(removeNthFromEnd(head, 1));
    }
}
/*
0   2
1
2
1->2->3->4->5->N
B        A

1


 */