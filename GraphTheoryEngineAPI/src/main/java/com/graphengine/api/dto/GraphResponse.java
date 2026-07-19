package com.graphengine.api.dto;

import java.util.List;
import java.util.Map;

public record GraphResponse(
        String type,
        int vertexCount,
        Map<Integer, List<Integer>> neighbours
) {}
