package com.example.algo.graph;
//https://leetcode.com/problems/number-of-islands/
public class NumOfIslands {
    static int[][] dirs = {{-1,0}, {1,0}, {0,-1}, {0, 1}};
    public static int numIslands(char[][] grid) {
        int count = 0;
        for(int i =0; i< grid.length; i++){
            for(int j =0; j< grid[0].length; j++){
                if (grid[i][j] == '1'){
                    dfs(grid, i, j);
                    count++;
                }
            }
        }
        return count;
    }

    static void  dfs(char[][] grid, int x, int y){
        if (x >= grid.length || x < 0 || y >= grid[0].length || y < 0 ) return;
        if (grid[x][y] != '1') return;
        grid[x][y] = 'v';
        for (int [] d: dirs){
            dfs(grid, x + d[0], y + d[1]);
        }
    }

    public static void main(String[] args) {
        char[][] input = {{'1','1','1','1','0'},{'1','1','0','1','0'},{'1','1','0','0','0'},{'0','0','0','0','0'}};
        System.out.println(numIslands(input));

    }
}
