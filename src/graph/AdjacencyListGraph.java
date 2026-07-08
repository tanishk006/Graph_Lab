package graph;

import java.util.ArrayList;
import java.util.HashMap;



class AdjacencyListGraph implements Graph {

      HashMap<Integer ,ArrayList<Edge>> adglist = new HashMap<>();


    public boolean addEdge(int source, int destination, double weight) {

        Edge newed = new Edge(destination , weight);

    adglist.putIfAbsent(source ,new ArrayList<>());
    ArrayList<Edge> list = adglist.get(source);
    list.add(newed);

    return true;

    }

   public ArrayList<Integer> getNeighbours(int vertex){
        ArrayList<Edge> edges = adglist.get(vertex);
        ArrayList<Integer> result = new ArrayList<>();

        for(Edge e : edges){
        result.add(e.vertice);
        }
        return result;
   }

   public int getVertexCount(){
        return adglist.size();
   }



}


