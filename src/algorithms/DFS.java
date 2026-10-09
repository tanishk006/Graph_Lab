package algorithms;

import graph.Graph;

import java.util.ArrayList;

public class DFS {
    private DFS() {}

    /** Vertices in the order DFS first reaches them, starting at start. */
    public static ArrayList<Integer> traverse(Graph g, int start) {
        ArrayList<Integer> order = new ArrayList<>();
        int n = g.getVertexCount();
        if (start < 0 || start >= n) {
            return order;
        }
        visit(g, start, new boolean[n], order);
        return order;
    }

    // Recursive: very deep graphs (thousands of vertices in a line) can overflow the call stack.
    private static void visit(Graph g, int u, boolean[] seen, ArrayList<Integer> order) {
        seen[u] = true;
        order.add(u);
        for (int v : g.getNeighbours(u)) {
            if (!seen[v]) {
                visit(g, v, seen, order);
            }
        }
    }
}
