package com.example.algo.heap;

import java.util.PriorityQueue;

public class KClosestPointsOfOrigin {

    public static int[][] kClosest(int[][] points, int k) {
        PriorityQueue<int[]> closestToOrigin = new PriorityQueue<>(
                (a,b)->Integer.compare(distSq(b), distSq(a)) ); //maxHeap
        for(var point: points){
            if (point != null) closestToOrigin.add(point);
            if (closestToOrigin.size() > k){
                closestToOrigin.poll();
            }
        }
        int[][] ret = new int[k][];
        int i = 0;
        while(!closestToOrigin.isEmpty()){
            ret[i++] = closestToOrigin.poll();
        }
        return ret;
    }


    static int distSq(int[] x){
        return x[0]*x[0] +x[1]*x[1];
    }

    public static void main(String[] args) {
        kClosest(new int[][]{}, 1);
        kClosest(new int[][]{{}}, 1);
        kClosest(new int[][]{{1,3}, {-2,2}}, 1); // [[1,3],[-2,2]]
        kClosest(new int[][]{{3,3}, {5,-1}, {-2,4}}, 2); // [[3,3],[5,-1],[-2,4]]
    }
}
/*
| Problem Input | Java Representation         |
| ------------- | --------------------------- |
| `[]`          | `new int[][]{}`             |
| `[[]]`        | `new int[][]{ {} }`         |
| `[[], []]`    | `new int[][]{ {}, {} }`     |
| `[[1,2],[3]]` | `new int[][]{ {1,2}, {3} }` |

 */