package com.example.algo.tree;

public class Diameter {

    // NOTE: 👉 LeetCode mostly uses node-based depth/height, not edge-based.
    //So if you use edge-based height, your answer can be off by 1 unless you adjust the formula.

    // https://leetcode.com/problems/diameter-of-binary-tree/description/
    public int diameterOfBinaryTree(TreeNode root) {
        return dfs(root)[0];
    }

    // returns int[] => diameter at index 0, height at index 1
    private int[] dfs(TreeNode root){
        if (root == null) return new int[]{0, 0};

        int[] L = dfs(root.left);
        int[] R = dfs(root.right);

        int diameter = Math.max(
                Math.max(L[0], R[0]),
                (L[1] + R[1])
        );
        int height = Math.max(L[1], R[1]) + 1;
        return new int[] {diameter, height};
    }



//    https://leetcode.com/problems/maximum-depth-of-binary-tree/
    // this is dfs
    public int maxDepth(TreeNode root) {
        if (root == null) return 0;
        return Math.max(maxDepth(root.left), maxDepth(root.right)) + 1;

    }
}
