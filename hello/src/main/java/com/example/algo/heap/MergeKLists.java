package com.example.algo.heap;

import com.example.algo.linkedList.ListNode;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.PriorityQueue;

public class MergeKLists {

    //You are given an array of k linked-lists lists, each linked-list is sorted in ascending order.
    //
    //Merge all the linked-lists into one sorted linked-list and return it.
    public static ListNode mergeKLists(ListNode[] lists) {
        PriorityQueue<ListNode> minHeap = new PriorityQueue<>((a,b) -> Integer.compare(a.val, b.val));
        for (int i = 0; i <lists.length; i++){
            if (lists[i] != null) minHeap.add(lists[i]);
        }
        ListNode dummy = new ListNode(0);
        ListNode ptr = dummy;
        while(!minHeap.isEmpty()){
            ListNode polled = minHeap.poll();
            ptr.next = polled;
            ptr = polled;
            if (ptr.next != null)
                minHeap.add(ptr.next);
        }
        return dummy.next;
    }

    public static void main(String[] args) {
        //lists = [[1,4,5],[1,3,4],[2,6]]
        //Output: [1,1,2,3,4,4,5,6]
        var l1 = ListNode.createList(1,4,5);
        var l2 = ListNode.createList(1,3,4);
        var l3 = ListNode.createList(2,6);
        ListNode listNode = mergeKLists(new ListNode[]{l1, l2, l3});
        ListNode.print(listNode);
        listNode = mergeKLists(new ListNode[]{null});
        ListNode.print(listNode);

    }
}
