package com.example.algo.tree;

public class LCA {
     /*
           3
         /   \
        5     1
       / \   / \
      6   2 0   8
         / \
        7   4
   */
    //https://leetcode.com/problems/lowest-common-ancestor-of-a-binary-tree/
    public TreeNode lowestCommonAncestor(TreeNode root, TreeNode p, TreeNode q) {
        if (root == null) return null;
        if (root == p || root == q) return root;

        var L = lowestCommonAncestor(root.left, p, q);
        var R = lowestCommonAncestor(root.right, p, q);

        if (L != null && R != null) return root;
        else if (L != null) return L;
        else return R;
    }
}
