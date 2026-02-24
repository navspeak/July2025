package com.example.algo.tree;

public class TiltDFS {
    public int findTilt(TreeNode root) {
        return helper(root)[0];
    }

    // https://leetcode.com/problems/binary-tree-tilt/description/
    private int[] helper(TreeNode node) {
        if (node == null) return new int[]{0, 0}; // {totalTilt, sum}

        int[] L = helper(node.left);
        int[] R = helper(node.right);

        int leftTilt = L[0], leftSum = L[1];
        int rightTilt = R[0], rightSum = R[1];

        int sum = leftSum + rightSum + node.val;
        int tiltHere = Math.abs(leftSum - rightSum);

        int totalTilt = leftTilt + rightTilt + tiltHere;

        return new int[]{totalTilt, sum};
    }
}
