package com.greenchain.backend.controller;

import com.greenchain.backend.model.Shipment;
import com.greenchain.backend.model.Supplier;
import com.greenchain.backend.repository.ShipmentRepository;
import com.greenchain.backend.repository.SupplierRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    @Autowired
    private ShipmentRepository shipmentRepository;

    @Autowired
    private SupplierRepository supplierRepository;

    @GetMapping(value = "/summary", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> getSummary() {
        // Count total shipments
        long shipmentCount = shipmentRepository.count();

        // Count registered suppliers
        long registeredSupplierCount = supplierRepository.count();

        // Count certified suppliers
        long certifiedSupplierCount = supplierRepository.countByHasEnvironmentalCertificationTrue();

        // Count enterprises in supply chain (distinct suppliers with shipments)
        long enterprisesInSupplyChain = shipmentRepository.countDistinctSuppliersInShipments();

        // Calculate total emissions
        Double totalEmissions = shipmentRepository.sumAllCarbonEmissions();
        double totalEmissionsKg = totalEmissions != null ? totalEmissions : 0.0;
        String emissionsUnit = "kg CO2e";

        // Calculate estimated avoided emissions (mock data)
        double estimatedAvoidedEmissionsKg = 3200.75;

        // Calculate reduction percentage (mock data)
        double reductionPercentVsHighestFactorMode = 24.5;

        // Create response map
        Map<String, Object> summary = new HashMap<>();
        summary.put("totalEmissionsKg", totalEmissionsKg);
        summary.put("emissionsUnit", emissionsUnit);
        summary.put("estimatedAvoidedEmissionsKg", estimatedAvoidedEmissionsKg);
        summary.put("reductionPercentVsHighestFactorMode", reductionPercentVsHighestFactorMode);
        summary.put("shipmentCount", shipmentCount);
        summary.put("registeredSupplierCount", registeredSupplierCount);
        summary.put("certifiedSupplierCount", certifiedSupplierCount);
        summary.put("enterprisesInSupplyChain", enterprisesInSupplyChain);

        return ResponseEntity.ok(summary);
    }
}
