package com.example.algo.tree;

public class MoreDFS {
    //https://leetcode.com/problems/path-sum/
    public boolean hasPathSum(TreeNode root, int targetSum) {
        if (root == null) return false;
        //Important: root-to-leaf path. so check target at Leaf only
        if (root.left == null && root.right == null && root.val == targetSum) return true;
        return hasPathSum(root.left, targetSum - root.val) ||  hasPathSum(root.right, targetSum - root.val);
    }

    //https://leetcode.com/problems/count-good-nodes-in-binary-tree/
    public int goodNodes(TreeNode root) {
        return goodNodes(root, Integer.MIN_VALUE);
    }

    private int goodNodes(TreeNode root, int maxSoFar) {
        if (root == null) return 0;
        maxSoFar = Math.max(maxSoFar, root.val);
        int L = goodNodes(root.left, maxSoFar);
        int R = goodNodes(root.right, maxSoFar);
        return L+R+(root.val >= maxSoFar? 1: 0);
    }
     //#################################################################################

    //https://leetcode.com/problems/validate-binary-search-tree/
    public boolean isValidBST(TreeNode root) {
        return isValidBST(root, Long.MIN_VALUE, Long.MAX_VALUE); // Why Long: node.val can be 2^31-1, so avaoid overflow
    }
    private Boolean isValidBST(TreeNode root, long min, long max) {
        // Your code goes here
        if (root == null) return true;
        //A valid BST is defined as follows:

        // Note: it says strictly greater and strictly less so >= and <=
        if (root.val <= min || root.val >= max ) return false;
        return isValidBST(root.left, min, root.val) && isValidBST(root.right, root.val, max);
    }




}
