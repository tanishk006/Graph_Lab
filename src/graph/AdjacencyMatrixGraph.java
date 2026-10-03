package graph;

import java.util.ArrayList;
import java.util.Arrays;

class AdjacencyMatrixGraph implements Graph {
    private final double[][] matrix;

    AdjacencyMatrixGraph(int vertexco) {
        if (vertexco < 0) {
            throw new IllegalArgumentException("Vertex count cannot be negative");
        }
        this.matrix = new double[vertexco][vertexco];
        for (double[] row : matrix) {
            Arrays.fill(row, Double.POSITIVE_INFINITY);
        }
    }

    private boolean isValid(int vertex) {
        return vertex >= 0 && vertex < matrix.length;
    }

    @Override
    public boolean addEdge(int source, int destination, double weight) {
        if (!isValid(source) || !isValid(destination) || Double.isNaN(weight)) {
            return false;
        }
        matrix[source][destination] = weight;
        return true;
    }

    @Override
    public ArrayList<Integer> getNeighbours(int vertex) {
        ArrayList<Integer> result = new ArrayList<>();
        if (!isValid(vertex)) {
            return result;
        }
        for (int i = 0; i < matrix.length; i++) {
            if (matrix[vertex][i] < Double.POSITIVE_INFINITY) {
                result.add(i);
            }
        }
        return result;
    }

    @Override
    public double getWeight(int source, int destination) {
        if (!isValid(source) || !isValid(destination)) {
            return Double.POSITIVE_INFINITY;
        }
        return matrix[source][destination];
    }

    @Override
    public ArrayList<Edge> getEdges(int vertex) {
        ArrayList<Edge> result = new ArrayList<>();
        if (!isValid(vertex)) {
            return result;
        }
        for (int i = 0; i < matrix.length; i++) {
            if (matrix[vertex][i] < Double.POSITIVE_INFINITY) {
                result.add(new Edge(i, matrix[vertex][i]));
            }
        }
        return result;
    }

    @Override
    public int getVertexCount() {
        return matrix.length;
    }
}