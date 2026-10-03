package graph;

import java.util.ArrayList;

class AdjacencyListGraph implements Graph {

    private final ArrayList<ArrayList<Edge>> adjList;

    AdjacencyListGraph(int vertexCount) {
        if (vertexCount < 0) {
            throw new IllegalArgumentException("Vertex count cannot be negative");
        }
        adjList = new ArrayList<>(vertexCount);
        for (int i = 0; i < vertexCount; i++) {
            adjList.add(new ArrayList<>());
        }
    }

    private boolean isValid(int vertex) {
        return vertex >= 0 && vertex < adjList.size();
    }

    @Override
    public boolean addEdge(int source, int destination, double weight) {
        if (!isValid(source) || !isValid(destination) || Double.isNaN(weight)) {
            return false;
        }
        ArrayList<Edge> list = adjList.get(source);
        Edge newEdge = new Edge(destination, weight);

        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).vertice == destination) {
                list.set(i, newEdge);
                return true;
            }
        }
        list.add(newEdge);
        return true;
    }

    @Override
    public ArrayList<Integer> getNeighbours(int vertex) {
        ArrayList<Integer> result = new ArrayList<>();
        if (!isValid(vertex)) {
            return result;
        }
        for (Edge e : adjList.get(vertex)) {
            result.add(e.vertice);
        }
        return result;
    }

    @Override
    public double getWeight(int source, int destination) {
        if (!isValid(source) || !isValid(destination)) {
            return Double.POSITIVE_INFINITY;
        }
        for (Edge e : adjList.get(source)) {
            if (e.vertice == destination) {
                return e.weight;
            }
        }
        return Double.POSITIVE_INFINITY;
    }

    @Override
    public ArrayList<Edge> getEdges(int vertex) {
        if (!isValid(vertex)) {
            return new ArrayList<>();
        }
        return new ArrayList<>(adjList.get(vertex));
    }

    @Override
    public int getVertexCount() {
        return adjList.size();
    }
}