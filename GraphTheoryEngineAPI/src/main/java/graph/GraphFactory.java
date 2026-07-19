package graph;

/**
 * Public entry point for creating Graph instances from outside the graph package.
 * AdjacencyListGraph and AdjacencyMatrixGraph are intentionally left package-private
 * (as originally written) — this factory is the only bridge the API layer uses.
 */
public class GraphFactory {

    public static Graph createAdjacencyListGraph() {
        return new AdjacencyListGraph();
    }

    public static Graph createAdjacencyMatrixGraph(int vertexCount) {
        return new AdjacencyMatrixGraph(vertexCount);
    }
}
