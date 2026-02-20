package com.example.algo.tree;

import java.util.*;

public class TraversalUsingStack {
    public List<Integer> preOrderTraversal(TreeNode root) {
        Deque<TreeNode> stack = new ArrayDeque<>();
        List<Integer> ret = new ArrayList<>();
        stack.push(root);
        while(!stack.isEmpty()){
            var curr = stack.pop();
            ret.add(curr.val);
            // stack is LIFO. We want left printed before right. so we push right first
            if (curr.right != null) stack.push(curr.right);
            if (curr.left != null) stack.push(curr.left);
        }
        return ret;
    }

    public List<Integer> inOrderTraversal(TreeNode root) {
        Deque<TreeNode> stack = new ArrayDeque<>();
        List<Integer> ret = new ArrayList<>();
        TreeNode curr = root;
        /*
                     1
                       2
                     3   4
                            5
         */
        while(curr != null && !stack.isEmpty()){
            // go all the way left
            while (curr != null) {
                stack.push(curr);
                curr = curr.left;
            }

            // process node
            curr = stack.pop();
            ret.add(curr.val);

            // go right
            curr = curr.right;
        }
        return ret;
    }

    public List<Integer> postOrderTraversal(TreeNode root) {
        LinkedList<Integer> ret = new LinkedList<>();
        if (root == null) return ret;

        Deque<TreeNode> stack = new ArrayDeque<>();
        stack.push(root);

        while (!stack.isEmpty()) {
            TreeNode curr = stack.pop();
            ret.addFirst(curr.val);  // reverse order

            if (curr.left != null) stack.push(curr.left);
            if (curr.right != null) stack.push(curr.right);
        }

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
        TraversalUsingStack t = new TraversalUsingStack();

        System.out.println(Arrays.toString(t.preOrderTraversal(one).toArray()));
        System.out.println(Arrays.toString(t.inOrderTraversal(one).toArray()));

    }
}
