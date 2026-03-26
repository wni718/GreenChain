package com.greenchain.backend.controller;

import com.greenchain.backend.model.Shipment;
import com.greenchain.backend.repository.ShipmentRepository;
import com.greenchain.backend.service.CarbonCalculationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/shipments")
public class ShipmentController {

    @Autowired
    private ShipmentRepository shipmentRepository;

    @Autowired
    private CarbonCalculationService carbonService;

    @GetMapping
    public List<Shipment> getAllShipments() {
        return shipmentRepository.findAll();
    }

    @PostMapping
    public Shipment createShipment(@RequestBody Shipment shipment) {
        // Automatically calculate carbon emissions during creation
        return carbonService.calculateShipmentEmission(shipment);
    }

    @GetMapping("/supplier/{supplierId}")
    public List<Shipment> getShipmentsBySupplier(@PathVariable Long supplierId) {
        return shipmentRepository.findBySupplierId(supplierId);
    }
}