package com.example.algo.tree;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Traversal {
    public List<Integer> preOrderTraversal(TreeNode root) {
        ArrayList<Integer> ret = new ArrayList<>();
        if (root == null) return ret;
        ret.add(root.val);
        ret.addAll(preOrderTraversal(root.left));
        ret.addAll(preOrderTraversal(root.right));
        return ret;
    }

    public List<Integer> inorderTraversal(TreeNode root) {
        if (root == null) return new ArrayList<>();
        var ret = inorderTraversal(root.left);
        ret.add(root.val);
        ret.addAll(inorderTraversal(root.right));
        return ret;
    }

    public List<Integer> postOrderTraversal(TreeNode root) {
        if (root == null) return new ArrayList<>();
        var ret = postOrderTraversal(root.left);
        ret.addAll(postOrderTraversal(root.right));
        ret.add(root.val);
        return ret;
    }

    public static void main(String[] args) {
//    #     1
//    #    / \
//    #   2   3
//    #  / \
//    # 4   5
        var one = new TreeNode(1);
        var two = new TreeNode(2);
        var three = new TreeNode(3);
        var four = new TreeNode(4);
        var five = new TreeNode(5);
        one.left = two; one.right = three;
        two.left = four; two.right = five;
        Traversal t = new Traversal();
        System.out.println(Arrays.toString(t.inorderTraversal(one).toArray()));
        System.out.println(Arrays.toString(t.preOrderTraversal(one).toArray()));
        System.out.println(Arrays.toString(t.postOrderTraversal(one).toArray()));

        /*
[4, 2, 5, 1, 3]
[1, 2, 4, 5, 3]
[4, 5, 2, 3, 1]
         */
    }
}
