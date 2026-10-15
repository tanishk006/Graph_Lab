package algorithms;

import graph.Graph;
import graph.GraphFactory;
import org.junit.jupiter.api.Test;
import utils.MstEdge;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class GraphAlgorithmsTest {

    // Helper to build reference graph
    private Graph buildReferenceGraph(boolean listBased) {
        Graph g = listBased
                ? GraphFactory.createAdjacencyListGraph(6)
                : GraphFactory.createAdjacencyMatrixGraph(6);

        // Undirected edges:
        // A(0)-B(1) 4, A(0)-C(2) 2, B(1)-C(2) 1, B(1)-D(3) 5
        // C(2)-E(4) 7, D(3)-E(4) 4, D(3)-F(5) 3, E(4)-F(5) 2
        int[][] edges = {
                {0, 1, 4},
                {0, 2, 2},
                {1, 2, 1},
                {1, 3, 5},
                {2, 4, 7},
                {3, 4, 4},
                {3, 5, 3},
                {4, 5, 2}
        };

        for (int[] e : edges) {
            g.addEdge(e[0], e[1], e[2]);
            g.addEdge(e[1], e[0], e[2]);
        }
        return g;
    }

    @Test
    public void testReferenceGraphDijkstra() {
        for (boolean listBased : new boolean[]{true, false}) {
            Graph g = buildReferenceGraph(listBased);
            Dijkstra.Result res = Dijkstra.run(g, 0);

            assertArrayEquals(new double[]{0, 3, 2, 8, 9, 11}, res.dist, 1e-9);

            ArrayList<Integer> path = Dijkstra.path(res, 0, 5);
            // Path cost must be 11, valid options are A-C-B-D-F or A-C-E-F
            double pathCost = 0;
            for (int i = 0; i < path.size() - 1; i++) {
                pathCost += g.getWeight(path.get(i), path.get(i + 1));
            }
            assertEquals(11.0, pathCost, 1e-9);
            assertEquals(0, path.get(0));
            assertEquals(5, path.get(path.size() - 1));
        }
    }

    @Test
    public void testReferenceGraphBellmanFord() {
        for (boolean listBased : new boolean[]{true, false}) {
            Graph g = buildReferenceGraph(listBased);
            double[] dist = BellmanFord.shortestDistances(g, 0);
            assertArrayEquals(new double[]{0, 3, 2, 8, 9, 11}, dist, 1e-9);
        }
    }

    @Test
    public void testReferenceGraphKruskal() {
        for (boolean listBased : new boolean[]{true, false}) {
            Graph g = buildReferenceGraph(listBased);
            ArrayList<MstEdge> tree = Kruskal.spanningForest(g);

            assertEquals(5, tree.size(), "MST must have 5 edges for 6 vertices");
            assertEquals(13.0, Kruskal.totalWeight(tree), 1e-9, "MST weight must be 13");
        }
    }

    @Test
    public void testReferenceGraphPrim() {
        for (boolean listBased : new boolean[]{true, false}) {
            Graph g = buildReferenceGraph(listBased);
            ArrayList<MstEdge> tree = Prim.spanningTree(g, 0);

            assertEquals(5, tree.size(), "Prim MST must have 5 edges for 6 vertices");
            double total = 0;
            for (MstEdge e : tree) total += e.weight();
            assertEquals(13.0, total, 1e-9, "Prim MST weight must be 13");
        }
    }

    @Test
    public void testReferenceGraphFloydWarshall() {
        for (boolean listBased : new boolean[]{true, false}) {
            Graph g = buildReferenceGraph(listBased);
            double[][] d = FloydWarshall.allPairs(g);

            // Row 0 must match Dijkstra distances from A
            assertArrayEquals(new double[]{0, 3, 2, 8, 9, 11}, d[0], 1e-9);
            // Symmetry check for undirected graph
            for (int i = 0; i < 6; i++) {
                for (int j = 0; j < 6; j++) {
                    assertEquals(d[i][j], d[j][i], 1e-9);
                }
            }
        }
    }

    @Test
    public void testReferenceGraphBFS() {
        for (boolean listBased : new boolean[]{true, false}) {
            Graph g = buildReferenceGraph(listBased);
            ArrayList<Integer> order = BFS.traverse(g, 0);

            assertEquals(6, order.size());
            assertEquals(6, new java.util.HashSet<>(order).size(), "BFS must visit each node once");
            assertEquals(0, order.get(0));

            int[] hops = BFS.hops(g, 0);
            assertArrayEquals(new int[]{0, 1, 1, 2, 2, 3}, hops);
        }
    }

    @Test
    public void testReferenceGraphDFS() {
        for (boolean listBased : new boolean[]{true, false}) {
            Graph g = buildReferenceGraph(listBased);
            ArrayList<Integer> order = DFS.traverse(g, 0);

            assertEquals(6, order.size());
            assertEquals(6, new java.util.HashSet<>(order).size(), "DFS must visit each node once");
            assertEquals(0, order.get(0));
        }
    }

    @Test
    public void testReferenceGraphUnionFind() {
        UnionFind uf = new UnionFind(6);
        // Add MST edges
        int[][] mstEdges = {{1, 2}, {0, 2}, {4, 5}, {3, 5}, {1, 3}};
        for (int[] e : mstEdges) {
            uf.union(e[0], e[1]);
        }
        assertEquals(1, uf.count(), "All six nodes must be in 1 set after MST");
        for (int i = 1; i < 6; i++) {
            assertTrue(uf.connected(0, i));
        }
    }

    // ------------------- STEP 3: EDGE CASES -------------------

    @Test
    public void testDisconnectedGraph() {
        Graph g = GraphFactory.createAdjacencyListGraph(3);
        g.addEdge(0, 1, 5.0);
        // Vertex 2 is isolated

        Dijkstra.Result dij = Dijkstra.run(g, 0);
        assertEquals(Double.POSITIVE_INFINITY, dij.dist[2]);
        assertTrue(Dijkstra.path(dij, 0, 2).isEmpty());

        double[] bf = BellmanFord.shortestDistances(g, 0);
        assertEquals(Double.POSITIVE_INFINITY, bf[2]);

        int[] hops = BFS.hops(g, 0);
        assertEquals(-1, hops[2]);

        ArrayList<MstEdge> prim = Prim.spanningTree(g, 0);
        assertEquals(1, prim.size(), "Prim on component 0 should only span reachable vertices");

        ArrayList<MstEdge> forest = Kruskal.spanningForest(g);
        assertEquals(1, forest.size(), "Kruskal forest should have 1 edge for 2 connected vertices");
    }

    @Test
    public void testSingleNode() {
        Graph g = GraphFactory.createAdjacencyListGraph(1);
        Dijkstra.Result dij = Dijkstra.run(g, 0);
        assertEquals(0.0, dij.dist[0]);
        assertEquals(List.of(0), Dijkstra.path(dij, 0, 0));

        double[] bf = BellmanFord.shortestDistances(g, 0);
        assertEquals(0.0, bf[0]);

        ArrayList<Integer> bfsOrder = BFS.traverse(g, 0);
        assertEquals(List.of(0), bfsOrder);

        ArrayList<MstEdge> prim = Prim.spanningTree(g, 0);
        assertTrue(prim.isEmpty());

        ArrayList<MstEdge> kruskal = Kruskal.spanningForest(g);
        assertTrue(kruskal.isEmpty());
    }

    @Test
    public void testEmptyGraph() {
        Graph g = GraphFactory.createAdjacencyListGraph(0);
        assertEquals(0, g.getVertexCount());
        assertTrue(BFS.traverse(g, 0).isEmpty());
        assertTrue(DFS.traverse(g, 0).isEmpty());
        assertThrows(IllegalArgumentException.class, () -> Dijkstra.run(g, 0));
        assertThrows(IllegalArgumentException.class, () -> BellmanFord.shortestDistances(g, 0));
        assertEquals(0, FloydWarshall.allPairs(g).length);
        assertTrue(Kruskal.spanningForest(g).isEmpty());
    }

    @Test
    public void testStartEqualsTarget() {
        Graph g = buildReferenceGraph(true);
        Dijkstra.Result dij = Dijkstra.run(g, 2);
        assertEquals(0.0, dij.dist[2]);
        assertEquals(List.of(2), Dijkstra.path(dij, 2, 2));
    }

    @Test
    public void testDirectedGraph() {
        Graph g = GraphFactory.createAdjacencyListGraph(3);
        g.addEdge(0, 1, 2.0);
        g.addEdge(1, 2, 3.0);
        // 2 cannot reach 0

        Dijkstra.Result dijFrom2 = Dijkstra.run(g, 2);
        assertEquals(Double.POSITIVE_INFINITY, dijFrom2.dist[0]);

        Dijkstra.Result dijFrom0 = Dijkstra.run(g, 0);
        assertEquals(5.0, dijFrom0.dist[2]);
        assertEquals(List.of(0, 1, 2), Dijkstra.path(dijFrom0, 0, 2));
    }

    @Test
    public void testNegativeWeights() {
        Graph g = GraphFactory.createAdjacencyListGraph(3);
        g.addEdge(0, 1, 4.0);
        g.addEdge(1, 2, -2.0);
        g.addEdge(0, 2, 5.0);

        // Dijkstra must reject negative weights
        assertThrows(IllegalArgumentException.class, () -> Dijkstra.run(g, 0));

        // Bellman-Ford must correctly handle them
        double[] bf = BellmanFord.shortestDistances(g, 0);
        assertEquals(2.0, bf[2], 1e-9);

        // Floyd-Warshall must correctly handle them
        double[][] fw = FloydWarshall.allPairs(g);
        assertEquals(2.0, fw[0][2], 1e-9);
    }

    @Test
    public void testNegativeCycle() {
        Graph g = GraphFactory.createAdjacencyListGraph(3);
        g.addEdge(0, 1, 1.0);
        g.addEdge(1, 2, -4.0);
        g.addEdge(2, 0, 1.0); // Cycle 0->1->2->0 has weight -2

        assertThrows(IllegalStateException.class, () -> BellmanFord.shortestDistances(g, 0));
        assertThrows(IllegalStateException.class, () -> FloydWarshall.allPairs(g));
    }

    @Test
    public void testDuplicateEdgesAndTies() {
        Graph gList = GraphFactory.createAdjacencyListGraph(2);
        gList.addEdge(0, 1, 5.0);
        gList.addEdge(0, 1, 3.0); // overwrites/updates to 3.0

        Graph gMatrix = GraphFactory.createAdjacencyMatrixGraph(2);
        gMatrix.addEdge(0, 1, 5.0);
        gMatrix.addEdge(0, 1, 3.0);

        assertEquals(3.0, gList.getWeight(0, 1));
        assertEquals(3.0, gMatrix.getWeight(0, 1));

        Dijkstra.Result dList = Dijkstra.run(gList, 0);
        Dijkstra.Result dMatrix = Dijkstra.run(gMatrix, 0);
        assertEquals(dList.dist[1], dMatrix.dist[1]);
    }

    @Test
    public void testGraphRepresentationEquivalence() {
        Graph listG = buildReferenceGraph(true);
        Graph matrixG = buildReferenceGraph(false);

        // Check BFS
        assertEquals(BFS.traverse(listG, 0), BFS.traverse(matrixG, 0));

        // Check DFS
        assertEquals(DFS.traverse(listG, 0), DFS.traverse(matrixG, 0));

        // Check Dijkstra
        assertArrayEquals(Dijkstra.run(listG, 0).dist, Dijkstra.run(matrixG, 0).dist, 1e-9);

        // Check Bellman-Ford
        assertArrayEquals(BellmanFord.shortestDistances(listG, 0), BellmanFord.shortestDistances(matrixG, 0), 1e-9);

        // Check Floyd-Warshall
        double[][] d1 = FloydWarshall.allPairs(listG);
        double[][] d2 = FloydWarshall.allPairs(matrixG);
        for (int i = 0; i < 6; i++) {
            assertArrayEquals(d1[i], d2[i], 1e-9);
        }

        // Check Kruskal MST weight
        assertEquals(Kruskal.totalWeight(Kruskal.spanningForest(listG)),
                Kruskal.totalWeight(Kruskal.spanningForest(matrixG)), 1e-9);

        // Check Prim MST weight
        double p1 = 0, p2 = 0;
        for (MstEdge e : Prim.spanningTree(listG, 0)) p1 += e.weight();
        for (MstEdge e : Prim.spanningTree(matrixG, 0)) p2 += e.weight();
        assertEquals(p1, p2, 1e-9);
    }
}
