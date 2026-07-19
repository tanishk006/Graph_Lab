package com.graphengine.api.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record EdgeDTO(
        @NotNull @Min(0) Integer source,
        @NotNull @Min(0) Integer destination,
        @NotNull Double weight
) {}
