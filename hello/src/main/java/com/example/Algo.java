package com.example;

import java.util.*;

public class Algo {

    String longestPalindromicSubstring(String s){
        if (s == null || s.length() == 0) return s;
        int start = 0, end = 0;
        int max = 1;
        for (int i = 0; i < s.length(); i++) {
            int l1 = expand(s, i, i);
            int l2 = expand(s, i, i+1);
            if (l1 > l2 && l1 > max){
                start = i - (l1-1)/2;
                end = i + (l1-1)/2;
                max = l1;

            } else if (l2>max){
                start = i - (l2-2)/2;
                end = i + 1 + (l2-2)/2;
                max=l2;
            }

        }
        return s.substring(start, end+1);
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

        int[] arr = new int[]{1,2,3, - 1};
        var len = arr.length;

        List.of().size();
        "Navneet".length();
        System.out.println("Navneet".indexOf('e'));

        List<Integer> lst =  new ArrayList<>(List.of(1, 2, 3, 4, 5));
        System.out.println(lst.indexOf(4));
        System.out.println(Arrays.toString(lst.subList(1, 3).toArray())); // 2, 3
        Arrays.sort(arr);
        System.out.println(Arrays.toString(arr));
        lst.sort(Comparator.reverseOrder());

        Queue<Integer> q = new ArrayDeque<>(3);
        System.out.println("Empty- null " + q.poll());
        try{
            System.out.println("Empty- Ex " + q.remove());
        }catch (NoSuchElementException e){
            System.out.println("NoSuchElementEx");
        }
        for (int i : List.of(1,2,3,4,5,6,7,8,9,10)){
            q.add(i);
            try {
                q.add(null);
                System.out.println(q.offer(null)); // also NPE
            } catch (NullPointerException e) {
                System.out.println("NoSuchElementEx");
            }
            if (q.size() == 3){
                System.out.println(Arrays.toString(q.toArray()));
                System.out.println("Removing " + q.remove()); // no such element execption
                System.out.println("Removing " + q.poll());
            }
        }
        System.out.println(Arrays.toString(q.toArray()));


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
