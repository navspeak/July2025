package com.example.algo.tree;

import java.util.*;

public class LevelOrderSum {
    public List<Integer> level_order_sum(TreeNode root) {
        List<Integer> res = new ArrayList<>();
        if (root == null) return res;
        Queue<TreeNode> q = new ArrayDeque<>();
        q.add(root);

        while (!q.isEmpty()){
            int size = q.size();
            int sum = 0;
            for (int i = 0; i < size; i++) {
                TreeNode curr = q.remove();
                sum+= curr.val;
                if (curr.left !=null) q.add(curr.left);
                if (curr.right !=null) q.add(curr.right);

            }
            res.add(sum);

        }
        return res;
    }
}
