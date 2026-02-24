package com.example.algo.tree;

import java.util.ArrayList;
import java.util.List;

public class PathSumII {

       // https://leetcode.com/problems/path-sum-ii/description/
      // IMPORTANT concepts: DFS + back tracking to remember the traversal nodes
        public List<List<Integer>> pathSum(TreeNode root, int targetSum) {
            List<List<Integer>> res = new ArrayList<>();
            getAllPathsWithTargetSum(root, targetSum, new ArrayList<>(), res);
            return res;

        }

        public void getAllPathsWithTargetSum(TreeNode root, int targetSum, List<Integer> path, List<List<Integer>> res){
            if (root == null) return;
            path.add(root.val);

            getAllPathsWithTargetSum(root.left, targetSum - root.val, path, res);
            getAllPathsWithTargetSum(root.right, targetSum - root.val, path, res);

            if (root.left == null && root.right == null && targetSum == root.val){
                res.add(new ArrayList<>(path)); // NOTE: can't add path as it will be reference. Need snapshot
            }
//            path.remove(Integer.valueOf(root.val)); - remove by value can be wrong if values are repeated
            path.remove(path.size() -1); // better still remove last index

        }

}
