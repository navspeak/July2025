package com.example;

import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

public class Algo {

    String longestPalindromicSubstring(String s){
        if (s == null) return "";
        int start =0;
        int end =0;
        int len = 1;
        for (int i = 0; i < s.length(); i++) {
            int len1 = expand(s, i, i);
            int len2 = expand(s, i, i+1);
            int newlen = Math.max(len1, len2);
            if (newlen > len){
                start = i - (newlen -1)/2;
                end = i + (newlen)/2;
                len = newlen;
            }
        }
        return s.substring(start, end);
    }

    public int expand(String s, int left, int right) {
        int l = left;
        int r = right;
        while(l>=0 && r<s.length() && s.charAt(l) == s.charAt(r)){
            l = l -1;
            r = r + 1;
        }
        return r -(l +1);
    }



    public ListNode mergeKLists(ListNode[] lists) {
        PriorityQueue<ListNode> pq = new PriorityQueue<>(Comparator.comparingInt(n -> n.val));

        for (var node : lists){
            if (node != null) pq.add(node);
        } //  O(K log k)
        ListNode dummy = new ListNode();
        var ptr = dummy;
        while(!pq.isEmpty()){//O(N logk)
            ListNode node = pq.remove();
            ptr.next= node;
            ptr = ptr.next;
            if (node.next !=null) pq.add(node.next);
        }
        ptr.next = null;
        return dummy.next;
    }


    public static void main(String[] args) {
        Algo algo = new Algo();
//        Input: lists = [[1,4,5],[1,3,4],[2,6]]
//        Output: [1,1,2,3,4,4,5,6]
        var l1 = algo.createLL(new int[]{1,4,5});
        var l2 = algo.createLL(new int[]{1,3,4});
        var l3 = algo.createLL(new int[]{2,6});
        ListNode merged = algo.mergeKLists(new ListNode[]{l1, l2, l3});
        merged.print();

    }

    ListNode createLL(int[] nums){
        ListNode ret = new ListNode();
        ListNode ptr = ret;
        for (int n : nums){
            ptr.next = new ListNode(n);
            ptr = ptr.next;
        }
        return ret.next;
    }

    public static class ListNode {
         int val;
         ListNode next;
         ListNode() {}
         ListNode(int val) { this.val = val; }
         ListNode(int val, ListNode next) { this.val = val; this.next = next; }
        void print(){
             ListNode ptr = this;
             StringBuilder sb = new StringBuilder();
             while(ptr !=null){
                 sb.append(ptr.val);
                 sb.append("->");
                 ptr = ptr.next;
             }
             sb.append("NULL");
            System.out.println(sb);
        }
        @Override
        public String toString(){
            ListNode ptr = this;
            StringBuilder sb = new StringBuilder();
            while(ptr !=null){
                sb.append(ptr.val);
                sb.append("->");
                ptr = ptr.next;
            }
            sb.append("NULL");
            return sb.toString();
        }
     }

}
