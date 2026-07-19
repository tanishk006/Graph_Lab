package com.graphengine.api.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.util.List;

public record BuildGraphRequest(

        // "list" -> AdjacencyListGraph, "matrix" -> AdjacencyMatrixGraph
        @NotNull @Pattern(regexp = "list|matrix", message = "type must be 'list' or 'matrix'")
        String type,

        // required only when type = "matrix" (array needs a fixed size upfront)
        @Min(1)
        Integer vertexCount,

        @NotEmpty
        List<@Valid EdgeDTO> edges

) {}
