package algorithms;

import graph.Edge;
import graph.Graph;
import utils.MstEdge;

import java.util.ArrayList;
import java.util.Comparator;

public class Kruskal {
    private Kruskal() {}

    /**
     * Minimum spanning forest. Treats the graph as undirected: for a graph built with an edge
     * in both directions, the second copy is simply rejected as a cycle.
     */
    public static ArrayList<MstEdge> spanningForest(Graph g) {
        int n = g.getVertexCount();
        ArrayList<MstEdge> all = new ArrayList<>();
        for (int u = 0; u < n; u++) {
            for (Edge e : g.getEdges(u)) {
                all.add(new MstEdge(u, e.getVertice(), e.getWeight()));
            }
        }
        all.sort(Comparator.comparingDouble(MstEdge::weight));

        UnionFind uf = new UnionFind(n);
        ArrayList<MstEdge> tree = new ArrayList<>();
        for (MstEdge e : all) {
            if (uf.union(e.from(), e.to())) {     // false means this edge would close a cycle
                tree.add(e);
                if (tree.size() == n - 1) {
                    break;
                }
            }
        }
        return tree;
    }

    public static double totalWeight(ArrayList<MstEdge> edges) {
        double sum = 0;
        for (MstEdge e : edges) {
            sum += e.weight();
        }
        return sum;
    }
}
