package com.example.algo.graph;

import java.util.*;

public class CloneGraph {

    /*
         1-----2
         |     |
         |     |
         4-----3
     */

    public Node cloneGraph(Node node) {
        if (node == null) return null;
        return cloneGraph_dfs(node, new HashMap<>());

    }
    public Node cloneGraph_dfs(Node node, Map<Node, Node> nodeMap) {
        if (nodeMap.containsKey(node)) return nodeMap.get(node);
        Node clonedNode = new Node(node.val);
        nodeMap.put(node, clonedNode);
        for (var neigbor: node.neighbors){
            clonedNode.neighbors.add(cloneGraph_dfs(neigbor, nodeMap));
        }
        return clonedNode;
    }

    public Node cloneGraph_bfs(Node node, Map<Node, Node> nodeMap) {
        Queue<Node> q = new ArrayDeque<>();
        q.add(node);
        nodeMap.put(node, new Node(node.val));
        while(!q.isEmpty()) {
            var curr = q.remove();
            var cloned = nodeMap.get(curr);
            for (var neighbor : curr.neighbors) {
                if (!nodeMap.containsKey(neighbor)) {
                    nodeMap.put(neighbor, new Node(neighbor.val));
                    q.add(neighbor);
                }
                cloned.neighbors.add(nodeMap.get(neighbor));
            }
        }
        return nodeMap.get(node);
    }
}
