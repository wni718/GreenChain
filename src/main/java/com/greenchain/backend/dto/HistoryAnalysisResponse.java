package com.greenchain.backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record HistoryAnalysisResponse(
    @JsonProperty("total_shipments") int totalShipments,
    @JsonProperty("total_carbon_emission") double totalCarbonEmission,
    @JsonProperty("average_emission_per_shipment") double averageEmissionPerShipment,
    @JsonProperty("most_used_transport_mode") String mostUsedTransportMode,
    @JsonProperty("lowest_carbon_transport_mode") String lowestCarbonTransportMode,
    @JsonProperty("potential_savings_percent") double potentialSavingsPercent,
    @JsonProperty("potential_savings_amount") double potentialSavingsAmount,
    @JsonProperty("recommendation") String recommendation
) {}