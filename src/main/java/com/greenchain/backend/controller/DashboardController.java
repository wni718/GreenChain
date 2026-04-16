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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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

        // Get emissions by shipment date
        List<Shipment> allShipments = shipmentRepository.findAll();
        Map<String, Double> emissionsByDateMap = new HashMap<>();
        Map<String, Double> emissionsByTransportModeMap = new HashMap<>();

        for (Shipment shipment : allShipments) {
            // Calculate emissions by date
            if (shipment.getShipmentDate() != null) {
                String date = shipment.getShipmentDate().toString();
                double emissions = shipment.getCalculatedCarbonEmission() != null
                        ? shipment.getCalculatedCarbonEmission()
                        : 0.0;
                emissionsByDateMap.put(date, emissionsByDateMap.getOrDefault(date, 0.0) + emissions);
            }

            // Calculate emissions by transport mode
            if (shipment.getTransportMode() != null && shipment.getTransportMode().getMode() != null) {
                String transportMode = shipment.getTransportMode().getMode().name();
                double emissions = shipment.getCalculatedCarbonEmission() != null
                        ? shipment.getCalculatedCarbonEmission()
                        : 0.0;
                emissionsByTransportModeMap.put(transportMode,
                        emissionsByTransportModeMap.getOrDefault(transportMode, 0.0) + emissions);
            }
        }

        // Convert maps to list of maps for response
        List<Map<String, Object>> emissionsByShipmentDate = new ArrayList<>();
        emissionsByDateMap.forEach((date, emissions) -> {
            Map<String, Object> map = new HashMap<>();
            map.put("date", date);
            map.put("emissionsKg", emissions);
            emissionsByShipmentDate.add(map);
        });

        List<Map<String, Object>> emissionsByTransportMode = new ArrayList<>();
        emissionsByTransportModeMap.forEach((transportMode, emissions) -> {
            Map<String, Object> map = new HashMap<>();
            map.put("transportMode", transportMode);
            map.put("emissionsKg", emissions);
            emissionsByTransportMode.add(map);
        });

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
        summary.put("emissionsByShipmentDate", emissionsByShipmentDate);
        summary.put("emissionsByTransportMode", emissionsByTransportMode);

        return ResponseEntity.ok(summary);
    }
}
