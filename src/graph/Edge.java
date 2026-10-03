package graph;

public class Edge {
    final int vertice;
    final double weight;

    Edge(int vertice, double weight) {
        this.vertice = vertice;
        this.weight = weight;
    }

    public int getVertice() {
        return vertice;
    }

    public double getWeight() {
        return weight;
    }

    @Override
    public String toString() {
        return "->" + vertice + "(" + weight + ")";
    }
}