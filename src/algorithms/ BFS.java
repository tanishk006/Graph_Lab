package algorithms;

import graph.Graph;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Queue;

public class BFS {
    private BFS() {}

    /** Vertices in the order BFS visits them, starting at start. */
    public static ArrayList<Integer> traverse(Graph g, int start) {
        ArrayList<Integer> order = new ArrayList<>();
        int n = g.getVertexCount();
        if (start < 0 || start >= n) {
            return order;
        }
        boolean[] seen = new boolean[n];
        Queue<Integer> queue = new ArrayDeque<>();
        seen[start] = true;
        queue.add(start);

        while (!queue.isEmpty()) {
            int u = queue.poll();
            order.add(u);
            for (int v : g.getNeighbours(u)) {
                if (!seen[v]) {
                    seen[v] = true;      // mark when queued, not when polled, so nothing is queued twice
                    queue.add(v);
                }
            }
        }
        return order;
    }

    /** Fewest edges from start to every vertex; -1 means unreachable. */
    public static int[] hops(Graph g, int start) {
        int n = g.getVertexCount();
        int[] dist = new int[n];
        Arrays.fill(dist, -1);
        if (start < 0 || start >= n) {
            return dist;
        }
        Queue<Integer> queue = new ArrayDeque<>();
        dist[start] = 0;
        queue.add(start);

        while (!queue.isEmpty()) {
            int u = queue.poll();
            for (int v : g.getNeighbours(u)) {
                if (dist[v] == -1) {
                    dist[v] = dist[u] + 1;
                    queue.add(v);
                }
            }
        }
        return dist;
    }
}
