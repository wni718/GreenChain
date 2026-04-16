package com.greenchain.backend.controller;

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
            @RequestParam(value = "cargo_weight_tons", required = false) Double cargoWeightTons) {
        return carbonLogicService.recommendBestMode(currentMode, distanceKm, cargoWeightTons);
    }
}
