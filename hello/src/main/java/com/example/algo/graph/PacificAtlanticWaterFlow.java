package com.example.algo.graph;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PacificAtlanticWaterFlow {
    public List<List<Integer>> pacificAtlantic(int[][] heights) {
        int m = heights.length, n = heights[0].length;
        Set<String> po = new HashSet<>();
        Set<String> ao = new HashSet<>();
        for(int i = 0; i<n; i++){
            dfs(heights, 0, i, ao, heights[0][i] );
            dfs(heights, m-1, i, po, heights[m-1][i] );
        }

        for(int i = 0; i<m; i++){
            dfs(heights, i, 0, ao, heights[i][0] );
            dfs(heights, i, m-1, po, heights[i][n-1] );
        }

        po.retainAll(ao);

        List<List<Integer>> ret = new ArrayList<>();
        for (var e: po){
            ret.add(List.of(
                    Integer.valueOf(e.split(":")[0]),
                    Integer.valueOf(e.split(":")[1])
            ));
        }
        return ret;
    }


    public void dfs(int[][] heights, int x, int y, Set<String> visit, int prevHeight){
        int m = heights.length, n = heights[0].length;
        String key = x+":"+y;
        if (visit.contains(key)) return;
        if (x < 0 || x >=m || y< 0 || y >= n) return;
        if (heights[x][y] < prevHeight) return;
        visit.add(key);
        dfs(heights, x-1, y, visit, heights[x][y]);
        dfs(heights, x+1, y, visit, heights[x][y]);
        dfs(heights, x, y-1, visit, heights[x][y]);
        dfs(heights, x, y+1, visit, heights[x][y]);
    }

    public static void main(String[] args) {
        PacificAtlanticWaterFlow pacificAtlanticWaterFlow = new PacificAtlanticWaterFlow();
        int[][] hts = new int[][]{
                {1,2,2,3,5},
                {3,2,3,4,4},
                {2,4,5,3,1},
                {6,7,1,4,5},
                {5,1,1,2,4}

        };
        List<List<Integer>> lists = pacificAtlanticWaterFlow.pacificAtlantic(hts);
        System.out.println();
    }
}
