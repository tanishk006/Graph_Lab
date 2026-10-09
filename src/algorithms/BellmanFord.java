package algorithms;

import graph.Edge;
import graph.Graph;

import java.util.Arrays;

public class BellmanFord {
    private BellmanFord() {}

    /**
     * Cheapest cost from start to every vertex; negative weights are fine.
     * Throws IllegalStateException if a negative cycle is reachable from start.
     */
    public static double[] shortestDistances(Graph g, int start) {
        int n = g.getVertexCount();
        if (start < 0 || start >= n) {
            throw new IllegalArgumentException("Start vertex out of range");
        }
        double[] dist = new double[n];
        Arrays.fill(dist, Double.POSITIVE_INFINITY);
        dist[start] = 0;

        // A shortest path has at most n-1 edges, so n-1 passes over every edge is enough.
        for (int pass = 1; pass < n; pass++) {
            boolean changed = false;
            for (int u = 0; u < n; u++) {
                if (dist[u] == Double.POSITIVE_INFINITY) {
                    continue;
                }
                for (Edge e : g.getEdges(u)) {
                    if (dist[u] + e.getWeight() < dist[e.getVertice()]) {
                        dist[e.getVertice()] = dist[u] + e.getWeight();
                        changed = true;
                    }
                }
            }
            if (!changed) {
                break;
            }
        }

        // If anything can still improve, the walk is going around a negative cycle.
        for (int u = 0; u < n; u++) {
            if (dist[u] == Double.POSITIVE_INFINITY) {
                continue;
            }
            for (Edge e : g.getEdges(u)) {
                if (dist[u] + e.getWeight() < dist[e.getVertice()]) {
                    throw new IllegalStateException("Negative cycle reachable from vertex " + start);
                }
            }
        }
        return dist;
    }
}
