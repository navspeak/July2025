package com.example.algo.tree;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class ReconstructBT {
    /*  Assumption: all unique values
               10
          8          12
      6      9    11     13

      Preorder [10, 8, 6, 9, 12, 11, 13]
      InOrder  [6,  8, 9,10,11,12,13]

      [10] [6,  8, 9] [11,12,13]
      [8] [6] [9] | [12] [11] [13]
     */

    Map<Integer, Integer> inorderIndexMap = new HashMap<>();
    int preIndex = 0;

    public TreeNode buildTree(int[] preorder,  int[] inorder) {
        for(int i =0; i< inorder.length; i++){
            inorderIndexMap.put(inorder[i], i);
        }
        return buildTree(preorder, inorder, 0,inorder.length -1);
    }

    private TreeNode buildTree(int[] preorder, int[] inorder, int inL, int inR) {
        TreeNode root = new TreeNode(preorder[preIndex++]);
        int pos = inorderIndexMap.get(root.val);
        root.left = buildTree(preorder, inorder,  inL, pos-1 );
        root.right = buildTree(preorder, inorder, pos +1, inR );
        return root;
    }


}
