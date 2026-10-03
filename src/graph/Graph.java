package graph;

import java.util.ArrayList;

public interface Graph {

    boolean addEdge(int source, int destination, double weight);

    ArrayList<Integer> getNeighbours(int vertex);

    double getWeight(int source, int destination);

    ArrayList<Edge> getEdges(int vertex);

    int getVertexCount();

}