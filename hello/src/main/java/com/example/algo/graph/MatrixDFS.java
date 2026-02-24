package com.example.algo.graph;

import java.util.HashSet;
import java.util.Set;
// This is repeated in matrix folder too
public class MatrixDFS {

    public static void dfs(int[][] matrix) {
        Set<String> visited = new HashSet<>();
        // up, down, left, right
        int[][] directions = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};

        dfsHelper(0, 0, matrix, visited, directions);
    }
    private static void dfsHelper(int r, int c, int[][] matrix, Set<String> visited, int[][] directions) {
        String key = r + "," + c;
        if (visited.contains(key)) {
            return;
        }

        // check if the cell is out of bounds
        if (r < 0 || r >= matrix.length || c < 0 || c >= matrix[0].length) {
            return;
        }

        visited.add(key);
        for (int[] dir : directions) {
            dfsHelper(r + dir[0], c + dir[1], matrix, visited, directions);
        }
        return;
    }
}
