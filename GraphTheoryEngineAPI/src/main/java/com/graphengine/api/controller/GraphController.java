package com.graphengine.api.controller;

import com.graphengine.api.dto.BuildGraphRequest;
import com.graphengine.api.dto.EdgeDTO;
import com.graphengine.api.dto.GraphResponse;
import com.graphengine.api.service.StatsService;
import graph.Graph;
import graph.GraphFactory;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/graph")
@CrossOrigin(origins = "*") // open to all — no signup/auth, matches the "no login" goal
public class GraphController {

    private final StatsService statsService;

    public GraphController(StatsService statsService) {
        this.statsService = statsService;
    }

    /**
     * Builds a graph from the submitted edges and returns each vertex's
     * neighbours, computed straight from the core engine (AdjacencyListGraph
     * or AdjacencyMatrixGraph — same classes used in the standalone project).
     *
     * Stateless: nothing is saved. Each request builds its own graph,
     * inspects it, and throws it away once the response is sent.
     */
    @PostMapping("/build")
    public GraphResponse buildGraph(@Valid @RequestBody BuildGraphRequest request) {

        int vertexCount = resolveVertexCount(request);
        Graph graph = createGraph(request.type(), vertexCount);

        for (EdgeDTO edge : request.edges()) {
            graph.addEdge(edge.source(), edge.destination(), edge.weight());
        }

        Map<Integer, java.util.List<Integer>> neighbours = new LinkedHashMap<>();
        for (int vertex = 0; vertex < vertexCount; vertex++) {
            neighbours.put(vertex, graph.getNeighbours(vertex));
        }

        statsService.incrementAndGet();

        return new GraphResponse(request.type(), graph.getVertexCount(), neighbours);
    }

    private Graph createGraph(String type, int vertexCount) {
        if (type.equals("matrix")) {
            return GraphFactory.createAdjacencyMatrixGraph(vertexCount);
        }
        return GraphFactory.createAdjacencyListGraph();
    }

    /**
     * AdjacencyMatrixGraph needs vertexCount upfront (fixed-size array).
     * AdjacencyListGraph doesn't — so if the client didn't send one,
     * we derive it from the highest vertex number mentioned in the edges.
     */
    private int resolveVertexCount(BuildGraphRequest request) {
        if (request.vertexCount() != null) {
            return request.vertexCount();
        }

        int max = -1;
        for (EdgeDTO edge : request.edges()) {
            max = Math.max(max, Math.max(edge.source(), edge.destination()));
        }
        return max + 1;
    }
}
