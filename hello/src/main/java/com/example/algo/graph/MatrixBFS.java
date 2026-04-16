package com.example.algo.graph;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

public class MatrixBFS {

    public static void main(String[] args) {
        int[][] matrix = {
                {0, 0, 0},
                {0, 1, 1},
                {0, 1, 0}
        };
    }


    public void bfs(int[][] grid) {
        Set<String> visited = new HashSet<>();
        // up, down, left, right
        int[][] directions = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};
        Queue<int[]> queue = new LinkedList<>();
        queue.offer(new int[]{0, 0});
        visited.add("0,0");
        while (!queue.isEmpty()) {
            int[] current = queue.poll();
            int row = current[0], col = current[1];
            // enqueue neighbors
            for (int[] dir : directions) {
                int nRow = row + dir[0];
                int mCol = col + dir[1];
                // check bounds and if neighbor is visited
                if (nRow >= 0 && nRow < grid.length &&
                        mCol >= 0 && mCol < grid[0].length &&
                        !visited.contains(nRow + "," + mCol)) {
                    queue.offer(new int[]{nRow, mCol});
                    visited.add(nRow + "," + mCol);
                }
            }
        }
    }

}
