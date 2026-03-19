package com.greenchain.backend.controller;

import com.greenchain.backend.repository.ShipmentRepository;
import com.greenchain.backend.service.OptimizationService;
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

    @GetMapping("/total")
    public Map<String, Object> getTotalEmissions() {
        Double total = shipmentRepository.sumAllCarbonEmissions();
        Map<String, Object> result = new HashMap<>();
        result.put("totalEmissions", total != null ? total : 0);
        result.put("unit", "kg CO2e");
        return result;
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