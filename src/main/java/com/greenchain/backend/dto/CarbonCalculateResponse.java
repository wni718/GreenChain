package com.greenchain.backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record CarbonCalculateResponse(
    @JsonProperty("carbon_kg_co2e") double carbonKgCo2e,
    @JsonProperty("unit") String unit
) {}

