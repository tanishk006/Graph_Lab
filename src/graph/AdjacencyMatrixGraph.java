package graph;

import java.util.ArrayList;


class AdjacencyMatrixGraph implements Graph {
    double[][] matrix;

    AdjacencyMatrixGraph(int vertexco) {
        this.matrix = new double[vertexco][vertexco];
    }


    public boolean addEdge(int source, int destination, double weight) {
        matrix[source][destination] = weight;

        return true;
    }

    public ArrayList<Integer> getNeighbours(int vertex) {
        ArrayList<Integer> result = new ArrayList<>();
        for (int i = 0; i < getVertexCount(); i++) {
            if (matrix[vertex][i] < Double.POSITIVE_INFINITY) {
                result.add(i);
            }

        }

        return result;
    }


    public int getVertexCount() {
        return matrix.length;
    }
}


