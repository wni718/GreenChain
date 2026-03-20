package com.greenchain.backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record RecommendResponse(
    @JsonProperty("best_mode") String bestMode,
    @JsonProperty("saving") String saving
) {}

