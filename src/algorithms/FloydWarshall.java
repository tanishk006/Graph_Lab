package algorithms;

import graph.Graph;

public class FloydWarshall {
    private FloydWarshall() {}

    /**
     * d[i][j] = cheapest cost from i to j (infinity if no route).
     * Throws IllegalStateException if the graph has a negative cycle.
     */
    public static double[][] allPairs(Graph g) {
        int n = g.getVertexCount();
        double[][] d = new double[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                d[i][j] = (i == j) ? Math.min(0, g.getWeight(i, i)) : g.getWeight(i, j);
            }
        }

        // k is the vertex we are allowed to pass THROUGH; keep k in the outermost loop.
        for (int k = 0; k < n; k++) {
            for (int i = 0; i < n; i++) {
                if (d[i][k] == Double.POSITIVE_INFINITY) {
                    continue;
                }
                for (int j = 0; j < n; j++) {
                    if (d[i][k] + d[k][j] < d[i][j]) {
                        d[i][j] = d[i][k] + d[k][j];
                    }
                }
            }
        }

        for (int i = 0; i < n; i++) {
            if (d[i][i] < 0) {
                throw new IllegalStateException("Negative cycle through vertex " + i);
            }
        }
        return d;
    }
}
