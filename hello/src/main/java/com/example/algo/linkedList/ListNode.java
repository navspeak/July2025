package com.example.algo.linkedList;

 public class ListNode {
     int val;
     ListNode next;

     ListNode() {
     }

     ListNode(int val) {
         this.val = val;
     }

     ListNode(int val, ListNode next) {
         this.val = val;
         this.next = next;
     }

     static ListNode createList(int... nums){
         ListNode dummy = new ListNode();
         ListNode ptr = dummy;
         for (int n : nums){
             ptr.next = new ListNode(n);
             ptr = ptr.next;
         }
         var ret = dummy.next;
         dummy = null;
         return ret;
     }

     static void print(ListNode head){
         StringBuilder sb = new StringBuilder();
         while(head!=null){
             sb.append(head.val);
             sb.append("->");
             head = head.next;
         }
         sb.append("Null");
         System.out.println(sb.toString());
     }
 }
