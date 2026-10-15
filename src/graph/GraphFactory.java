package graph;

/**
 * Public factory for creating Graph instances.
 * AdjacencyListGraph and AdjacencyMatrixGraph are package-private,
 * so this factory provides the clean bridge for external callers.
 */

public class GraphFactory {

    public static Graph createAdjacencyListGraph(int vertexCount) {
        return new AdjacencyListGraph(vertexCount);
    }

    public static Graph createAdjacencyMatrixGraph(int vertexCount) {
        return new AdjacencyMatrixGraph(vertexCount);
    }
}
