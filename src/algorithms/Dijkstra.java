package algorithms;

import graph.Edge;
import graph.Graph;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.PriorityQueue;

public class Dijkstra {
    private Dijkstra() {}

    /** dist[v] = cheapest cost start->v (infinity if unreachable); prev[v] = vertex before v on that route (-1 if none). */
    public static class Result {
        public final double[] dist;
        public final int[] prev;

        Result(double[] dist, int[] prev) {
            this.dist = dist;
            this.prev = prev;
        }
    }

    public static Result run(Graph g, int start) {
        int n = g.getVertexCount();
        if (start < 0 || start >= n) {
            throw new IllegalArgumentException("Start vertex out of range");
        }
        double[] dist = new double[n];
        int[] prev = new int[n];
        Arrays.fill(dist, Double.POSITIVE_INFINITY);
        Arrays.fill(prev, -1);
        dist[start] = 0;

        // entries are {vertex, distance when queued}
        PriorityQueue<double[]> pq = new PriorityQueue<>((a, b) -> Double.compare(a[1], b[1]));
        pq.add(new double[]{start, 0});

        while (!pq.isEmpty()) {
            double[] top = pq.poll();
            int u = (int) top[0];
            if (top[1] > dist[u]) {
                continue;                 // stale entry: a cheaper route to u was already found
            }
            for (Edge e : g.getEdges(u)) {
                if (e.getWeight() < 0) {
                    throw new IllegalArgumentException("Dijkstra cannot handle negative weights; use BellmanFord");
                }
                int v = e.getVertice();
                double candidate = dist[u] + e.getWeight();
                if (candidate < dist[v]) {
                    dist[v] = candidate;
                    prev[v] = u;
                    pq.add(new double[]{v, candidate});
                }
            }
        }
        return new Result(dist, prev);
    }

    /** Vertices from start to target in order, or an empty list if target is unreachable. */
    public static ArrayList<Integer> path(Result r, int start, int target) {
        ArrayList<Integer> path = new ArrayList<>();
        if (target < 0 || target >= r.dist.length || r.dist[target] == Double.POSITIVE_INFINITY) {
            return path;
        }
        for (int at = target; at != -1; at = r.prev[at]) {
            path.add(at);
        }
        Collections.reverse(path);
        return path;
    }
}
