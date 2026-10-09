package algorithms;

import graph.Edge;
import graph.Graph;
import utils.MstEdge;

import java.util.ArrayList;
import java.util.PriorityQueue;

public class Prim {
    private Prim() {}

    /**
     * Minimum spanning tree of the component containing start.
     * Expects an undirected graph (each edge added in both directions).
     */
    public static ArrayList<MstEdge> spanningTree(Graph g, int start) {
        int n = g.getVertexCount();
        ArrayList<MstEdge> tree = new ArrayList<>();
        if (start < 0 || start >= n) {
            return tree;
        }
        boolean[] inTree = new boolean[n];
        PriorityQueue<MstEdge> pq = new PriorityQueue<>((a, b) -> Double.compare(a.weight(), b.weight()));

        inTree[start] = true;
        for (Edge e : g.getEdges(start)) {
            pq.add(new MstEdge(start, e.getVertice(), e.getWeight()));
        }

        while (!pq.isEmpty()) {
            MstEdge best = pq.poll();             // cheapest edge leaving the tree so far
            if (inTree[best.to()]) {
                continue;                          // both ends already inside: skip
            }
            inTree[best.to()] = true;
            tree.add(best);
            for (Edge e : g.getEdges(best.to())) {
                if (!inTree[e.getVertice()]) {
                    pq.add(new MstEdge(best.to(), e.getVertice(), e.getWeight()));
                }
            }
        }
        return tree;
    }
}
