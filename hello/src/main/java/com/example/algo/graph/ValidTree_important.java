package com.example.algo.graph;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

//https://leetcode.com/problems/graph-valid-tree/description/
// https://www.lintcode.com/problem/178/
/*
Given n nodes labeled from 0 to n - 1 and
a list of undirected edges (each edge is a pair of nodes),
write a function to check whether these edges make up a valid tree.

A graph is a valid tree iff:
It has exactly n − 1 edges
It is connected (equivalently: no cycles + connected)
That’s the whole problem.

Fast checks
If edges.length != n - 1 → false immediately
(A tree on n nodes must have n-1 edges.)
Then you can verify connectivity using BFS/DFS, or detect cycles + connectivity using Union-Find.
 */
public class ValidTree_important {
    public boolean validTree(int n, int[][] edges) {
        if (n == 0) return true; // Empty Graph is a tree
        /*
| Case | edges count | Graph property                            |
| ---- | ----------- | ----------------------------------------- |
| 1    | `< n - 1`   | **Disconnected** (too few edges)          |
| 2    | `= n - 1`   | Could be a tree (need to check connected) |
| 3    | `> n - 1`   | **Must have a cycle** (extra edges)       |

 */
        if (edges.length != n-1) return false; // For n nodes there must be n-1 edges to be a tree
        List<List<Integer>> g = new ArrayList<>();
        // nodes are labeled from 0 to n - 1
        for (int i =0; i< n; i++){ // First create an emptyList =>
            g.add(new ArrayList<>());
        }
        for (int[] e : edges) {
            g.get(e[0]).add(e[1]);
            g.get(e[1]).add(e[0]);
        }
        boolean[] visited = new boolean[n];
        /*
        For an undirected graph with n nodes,
        if it has exactly n - 1 edges and is connected → it MUST be a tree (no cycles).
        so we are not explicitly checking loop
         */
        dfs(0,g, visited);

        // EXPLICIT CYCLE CHECK NOT NEEDED as we know from edges.length == n-1
        //if (hasCycle(0, Integer.MIN_VALUE, g, visited)) return false;

        for (boolean v: visited){
            if (!v) return false; // not connected
        }

        return true;
    }

    /*
    Complexity
    Time: O(V + E)
    Space: O(V + E) for graph + O(V) recursion stack worst-case
     */
    private void dfs(int u, List<List<Integer>> g, boolean[] visited) {
//        if (visited[u]) return; // NOT NEEDED as we always do dfs on unvisted here
        visited[u] = true;
        for(int v: g.get(u)){
            if (!visited[v]){
                dfs(v,g,visited);
            }
        }
    }

    private boolean hasCycle(int u, int parent, List<List<Integer>> g, boolean[] visited) {
        visited[u] = true;
        for (int v : g.get(u)) {
            if (!visited[v]) {
                if (hasCycle(v, u, g, visited)) return true;
            } else if (v != parent) {
                return true; // back-edge => cycle
            }
        }
        return false;
    }


    public boolean validTree_bfs(int n, int[][] edges) {
        if (n == 0) return true;              // depends on platform; usually n>=1
        if (edges.length != n - 1) return false;

        List<List<Integer>> g = new ArrayList<>();
        for (int i = 0; i < n; i++) g.add(new ArrayList<>());
        for (int[] e : edges) {
            g.get(e[0]).add(e[1]);
            g.get(e[1]).add(e[0]);
        }

        boolean[] vis = new boolean[n];
        Queue<Integer> q = new ArrayDeque<>();
        q.add(0);
        vis[0] = true;

        int seen = 1;
        while (!q.isEmpty()) {
            int u = q.remove();
            for (int v : g.get(u)) {
                if (!vis[v]) {
                    vis[v] = true;
                    seen++;
                    q.add(v);
                }
            }
        }

        return seen == n; // connected
    }



}

/*
Input: n = 5 edges = [[0, 1], [0, 2], [0, 3], [1, 4]]
Output: true.

Input: n = 5 edges = [[0, 1], [1, 2], [2, 3], [1, 3], [1, 4]]
Output: false.
g = [
  [],   // g[0] => [1,4]
  [],   // g[1] => [0,2,3,4]
  [],   // g[2] => [1,3]
  [],   // g[3] => [1]
  []    // g[4] => [1]
]
 */