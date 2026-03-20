package com.greenchain.backend.controller;

import com.greenchain.backend.dto.CarbonCalculateRequest;
import com.greenchain.backend.dto.CarbonCalculateResponse;
import com.greenchain.backend.repository.ShipmentRepository;
import com.greenchain.backend.service.OptimizationService;
import com.greenchain.backend.service.CarbonLogicService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/carbon")
public class CarbonController {

    @Autowired
    private ShipmentRepository shipmentRepository;

    @Autowired
    private OptimizationService optimizationService;

    private final CarbonLogicService carbonLogicService;

    @Autowired
    public CarbonController(CarbonLogicService carbonLogicService) {
        this.carbonLogicService = carbonLogicService;
    }

    @GetMapping("/total")
    public Map<String, Object> getTotalEmissions() {
        Double total = shipmentRepository.sumAllCarbonEmissions();
        Map<String, Object> result = new HashMap<>();
        result.put("totalEmissions", total != null ? total : 0);
        result.put("unit", "kg CO2e");
        return result;
    }

    /**
     * 最简单的碳排放计算接口：distance_km * emission_factor(mode) * cargo_weight_tons
     */
    @PostMapping("/calculate")
    public CarbonCalculateResponse calculateCarbon(@RequestBody CarbonCalculateRequest request) {
        return carbonLogicService.calculate(
                request.distanceKm(),
                request.cargoWeightTons(),
                request.mode()
        );
    }

    @GetMapping("/optimize/supplier/{supplierId}")
    public Map<String, Object> optimizeSupplier(@PathVariable Long supplierId) {
        return optimizationService.recommendGreenerSupplier(supplierId);
    }

    @GetMapping("/optimize/transport/{mode}")
    public Map<String, Object> optimizeTransport(@PathVariable String mode) {
        return optimizationService.recommendGreenerTransport(mode);
    }
}