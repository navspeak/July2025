package com.example.algo.tree;

import java.util.*;

public class RightSideView {
    public static List<Integer> rightSideView(TreeNode root) {
        List<Integer>  res = new ArrayList<>();
        Queue<TreeNode> q = new ArrayDeque<>();
        if (root == null) return res;
        q.add(root);

        while(!q.isEmpty()){
            int size = q.size();
            for (int i = 0; i < size; i++){
                var curr = q.remove();
                if (curr.left !=null) q.add(curr.left);
                if (curr.right!=null) q.add(curr.right);
                if (i == size-1) res.add(curr.val);
            }

        }
        return res;
    }

    public static void main(String[] args) {
        var one = new TreeNode(1);
        var two = new TreeNode(2);
        var three = new TreeNode(3);
        var four = new TreeNode(4);
        var five = new TreeNode(5);
        one.left = two; one.right = three;
        two.left = five; three.left = four;
        List<Integer> integers = rightSideView(one);
        System.out.println(Arrays.toString(integers.toArray()));
    }
}
