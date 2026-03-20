package com.greenchain.backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record CarbonCalculateRequest(
    @JsonProperty("distance_km") Double distanceKm,
    @JsonProperty("cargo_weight_tons") Double cargoWeightTons,
    @JsonProperty("mode") String mode
) {}

