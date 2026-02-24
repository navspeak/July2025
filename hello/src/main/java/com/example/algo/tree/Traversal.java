package com.example.algo.tree;

import java.util.*;

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

    // BFS
    /*
            1
        /       \
      2           3
    /   \       /   \
   4     5     6     7
  / \   / \   / \   / \
 8  9 10 11 12 13 14 15
 [
  [1],
  [3,2],
  [4,5,6,7],
  [15,14,13,12,11,10,9,8]
]
     */
    public List<List<Integer>> zigzagLevelOrder(TreeNode root) {
        List<List<Integer>> ret = new ArrayList<>();
        Queue<TreeNode> q = new ArrayDeque<>();
        q.add(root);
        int level = 1;
        while(!q.isEmpty()){
            List<Integer> thisLevel = new ArrayList<>();
            int size = q.size(); // IMPORTANT to not have it in loop below as we are adding to q and will change size
            for (int i = 0; i < size; i++ ) {
                var curr = q.remove();
                thisLevel.add(curr.val);
                if (curr.left != null) q.add(curr.left);
                if (curr.right != null) q.add(curr.right);
            }
            if (level++ % 2 == 0)  Collections.reverse(thisLevel); // still O(N)
            ret.add(thisLevel);

        }
        return ret;
    }

    public List<List<Integer>> zigzagLevelOrder_avoidReversing(TreeNode root) {
        if (root == null) return List.of();

        List<List<Integer>> ret = new ArrayList<>();
        Queue<TreeNode> q = new ArrayDeque<>();
        q.add(root);
        boolean leftToRight = true;

        while (!q.isEmpty()) {
            int size = q.size();
            Deque<Integer> levelList = new ArrayDeque<>();

            for (int i = 0; i < size; i++) {
                TreeNode curr = q.remove();

                if (leftToRight) {
                    levelList.addLast(curr.val);
                } else {
                    levelList.addFirst(curr.val);
                }

                if (curr.left != null) q.add(curr.left);
                if (curr.right != null) q.add(curr.right);
            }

            ret.add(new ArrayList<>(levelList));
            leftToRight = !leftToRight;
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
