package com.example.algo.tree;

public class LongestUniValue_ {
    int best =0;
    public int longestUnivaluePath(TreeNode root) {
        dfs(root);
        return best;
    }

    // returns longest path
    int dfs(TreeNode root){
        if (root == null) return 0;
        int left = dfs(root.left);
        int right = dfs(root.right);
        int leftChain=0, rightChain= 0;
        if (root.left != null && root.left.val == root.val){
            leftChain = left + 1;
        }

        if (root.right != null && root.right.val == root.val){
            rightChain = right+1;
        }

        best = Math.max(best, leftChain+rightChain);

        return Math.max(leftChain, rightChain);
    }

    /*
    does NOT have to start at the root
    does NOT have to end at a leaf
    does NOT even have to pass through the root
    */
}
