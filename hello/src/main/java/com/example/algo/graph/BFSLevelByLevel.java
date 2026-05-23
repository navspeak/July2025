package com.example.algo.graph;

import java.util.*;
//https://leetcode.com/problems/binary-tree-level-order-traversal/description/
public class BFSLevelByLevel {
    public List<List<String>> bfsLevels(Map<String, List<String>> graph, String start) {
        Queue<String> queue = new LinkedList<>();
        queue.offer(start);
        Set<String> visited = new HashSet<>();
        visited.add(start);
        List<List<String>> levels = new ArrayList<>();

        while (!queue.isEmpty()) {
            int levelSize = queue.size();
            List<String> currentLevel = new ArrayList<>();

            for (int i = 0; i < levelSize; i++) {
                String node = queue.poll();
                currentLevel.add(node);
                for (String neighbor : graph.get(node)) {
                    if (!visited.contains(neighbor)) {
                        visited.add(neighbor);
                        queue.offer(neighbor);
                    }
                }
            }

            // IMPORTANT
            // we have finished processing all nodes at the current level
            levels.add(currentLevel);
        }

        return levels;
    }

    public List<List<int[]>> bfsLevelByLevel(int[][] matrix) {
        int rows = matrix.length;
        int cols = matrix[0].length;
        int[][] directions = {{0, 1}, {1, 0}, {0, -1}, {-1, 0}};

        // start at the top-left corner
        Queue<int[]> queue = new LinkedList<>();
        queue.offer(new int[]{0, 0});
        Set<String> visited = new HashSet<>();
        visited.add("0,0");

        List<List<int[]>> levels = new ArrayList<>();
        while (!queue.isEmpty()) {
            int levelSize = queue.size();
            List<int[]> currentLevel = new ArrayList<>();

            for (int i = 0; i < levelSize; i++) {
                int[] pos = queue.poll();
                int row = pos[0], col = pos[1];
                currentLevel.add(new int[]{row, col});
                for (int[] dir : directions) {
                    int r = row + dir[0];
                    int c = col + dir[1];
                    if (r >= 0 && r < rows && c >= 0 && c < cols && !visited.contains(r + "," + c)) {
                        visited.add(r + "," + c);
                        queue.offer(new int[]{r, c});
                    }
                }
            }

            // IMPORTANT
            // we have finished processing all nodes at this level
            levels.add(currentLevel);
        }

        return levels;
    }
}
