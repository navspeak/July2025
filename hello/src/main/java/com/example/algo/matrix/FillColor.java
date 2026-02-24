package com.example.algo.matrix;

public class FillColor {
    //https://leetcode.com/problems/flood-fill/
    static int[][] directions =  {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};
    public int[][] floodFill(int[][] image, int sr, int sc, int color) {
        //Set<String> visited = new HashSet<>(); // Not needed
        int start = image[sr][sc];
        if (start == color) return image; // Important guard, without this it will be an infinite loop
        /* [[1,1],[1,1],[1,0]], sr = 0, sc = 0, color = 1 => test with this */
        dfs(image, sr, sc, start, color);
        return image;
    }

    void dfs(int[][] image, int sr, int sc, int start, int color){
        if (sr<0||sr>=image.length || sc<0 || sc>=image[0].length) return;
        if (image[sr][sc] != start) return; // will take care of visited as well
        image[sr][sc] = color;
        for (int[] dir: directions){
            dfs(image, sr + dir[0], sc + dir[1], start, color);
        }
    }
}
