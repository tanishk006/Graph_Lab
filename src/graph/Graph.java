package graph;
import java.util.ArrayList;

public interface Graph {

    boolean addEdge(int source, int destination , double weight);
    ArrayList<Integer> getNeighbours(int vertex);
    int getVertexCount();

}

