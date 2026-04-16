package com.greenchain.backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record RecommendResponse(
        @JsonProperty("best_mode") String bestMode,
        @JsonProperty("saving") String saving,
        @JsonProperty("saving_amount") double savingAmount,
        @JsonProperty("saving_amount_unit") String savingAmountUnit,
        @JsonProperty("current_emission") double currentEmission,
        @JsonProperty("recommended_emission") double recommendedEmission,
        @JsonProperty("time_factor") double timeFactor,
        @JsonProperty("cost_factor") double costFactor) {
    public RecommendResponse {
        if (savingAmountUnit == null) {
            savingAmountUnit = "kg CO2e";
        }
    }
}
