package com.greenchain.backend.controller;

import com.greenchain.backend.dto.HistoryAnalysisResponse;
import com.greenchain.backend.dto.RecommendResponse;
import com.greenchain.backend.service.CarbonLogicService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RecommendController {

    private final CarbonLogicService carbonLogicService;

    public RecommendController(CarbonLogicService carbonLogicService) {
        this.carbonLogicService = carbonLogicService;
    }

    @GetMapping("/api/recommend")
    public RecommendResponse recommend(
            @RequestParam("current_mode") String currentMode,
            @RequestParam(value = "distance_km", required = false) Double distanceKm,
            @RequestParam(value = "cargo_weight_tons", required = false) Double cargoWeightTons,
            @RequestParam(value = "supplier_id", required = false) Long supplierId) {
        return carbonLogicService.recommendBestMode(currentMode, distanceKm, cargoWeightTons, supplierId);
    }

    @GetMapping("/api/recommend/history-analysis")
    public HistoryAnalysisResponse analyzeHistory() {
        return carbonLogicService.analyzeHistoryData();
    }

    @GetMapping("/api/recommend/smart")
    public RecommendResponse smartRecommend(
            @RequestParam(value = "origin", required = false) String origin,
            @RequestParam(value = "destination", required = false) String destination,
            @RequestParam(value = "cargo_weight_tons", required = false) Double cargoWeightTons,
            @RequestParam(value = "supplier_id", required = false) Long supplierId) {
        return carbonLogicService.recommendBestModeFromHistory(origin, destination, cargoWeightTons, supplierId);
    }
}
