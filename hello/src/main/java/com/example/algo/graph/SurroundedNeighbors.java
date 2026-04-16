package com.example.algo.graph;

import java.util.ArrayDeque;
import java.util.Queue;

/*
You are given an m x n matrix board containing letters 'X' and 'O', capture regions that are surrounded:

Connect: A cell is connected to adjacent cells horizontally or vertically.
Region: To form a region connect every 'O' cell.
Surround: A region is surrounded if none of the 'O' cells in that region are on the edge of the board. Such regions are completely enclosed by 'X' cells.
To capture a surrounded region, replace all 'O's with 'X's in-place within the original board. You do not need to return anything.

 Input: board =
 ["X","X","X","X"],
 ["X","O","O","X"],
 ["X","X","O","X"],
 ["X","O","X","X"]]

Output:
["X","X","X","X"],
["X","X","X","X"],
["X","X","X","X"],
["X","O","X","X"]]

Question asks to convert the surrounded region of O to X
we can reverse think, convert everything except unsurrounded => touching border
 */

public class SurroundedNeighbors {
    private static final int[][] DIRS = {{-1,0}, {1,0}, {0,-1}, {0,1}};

    public void solve(char[][] board) {
        if (board == null || board.length == 0 || board[0].length == 0) return;

        int m = board.length, n = board[0].length;

        // 1) Mark all 'O' connected to the border as safe (use '#')
        // Top and bottom rows
        for (int j = 0; j < n; j++) {
            if (board[0][j] == 'O') dfs(board, 0, j);
            if (board[m - 1][j] == 'O') dfs(board, m - 1, j);
        }
        // Left and right columns
        for (int i = 0; i < m; i++) {
            if (board[i][0] == 'O') dfs(board, i, 0);
            if (board[i][n - 1] == 'O') dfs(board, i, n - 1);
        }

        // 2) Flip surrounded 'O' -> 'X', and restore safe '#' -> 'O'
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                if (board[i][j] == 'O') board[i][j] = 'X';
                else if (board[i][j] == '#') board[i][j] = 'O';
            }
        }
    }

    private void dfs(char[][] board, int x, int y) {
        int m = board.length, n = board[0].length;
        if (x < 0 || x >= m || y < 0 || y >= n) return;
        if (board[x][y] != 'O') return;

        board[x][y] = '#'; // mark safe / visited

        for (int[] d : DIRS) {
            dfs(board, x + d[0], y + d[1]);
        }
    }
}
/*

Input: board =
["X","X","X","X"],
["X","O","O","X"],
["X","X","O","X"],
["X","O","X","X"]

Output: [["X","X","X","X"],["X","X","X","X"],["X","X","X","X"],["X","O","X","X"]]
 */