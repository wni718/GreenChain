package com.greenchain.backend.service;

import com.greenchain.backend.model.Shipment;
import com.greenchain.backend.model.TransportMode;
import com.greenchain.backend.repository.ShipmentRepository;
import com.greenchain.backend.repository.SupplierRepository;
import com.greenchain.backend.repository.TransportModeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class CarbonCalculationService {

    @Autowired
    private ShipmentRepository shipmentRepository;

    @Autowired
    private SupplierRepository supplierRepository;

    @Autowired
    private TransportModeRepository transportModeRepository;

    // Carbon emissions=distance x emission factor x quantity
    public Double calculateEmission(Double distanceKm, Double emissionFactor, Double weightTons) {
        if (distanceKm == null || emissionFactor == null || weightTons == null) {
            return 0.0;
        }
        return distanceKm * emissionFactor * weightTons;
    }

    // Calculate the carbon emissions of a single transportation
    public Shipment calculateShipmentEmission(Shipment shipment) {
        // Ensure that the supplier and transportMode are complete objects
        if (shipment.getSupplier() != null && shipment.getSupplier().getId() != null) {
            shipment.setSupplier(supplierRepository.findById(shipment.getSupplier().getId()).orElse(null));
        }
        if (shipment.getTransportMode() != null && shipment.getTransportMode().getId() != null) {
            shipment.setTransportMode(transportModeRepository.findById(shipment.getTransportMode().getId()).orElse(null));
        }

        TransportMode mode = shipment.getTransportMode();
        if (mode != null && mode.getEmissionFactorPerKmPerTon() != null) {
            Double emission = calculateEmission(
                    shipment.getDistanceKm(),
                    mode.getEmissionFactorPerKmPerTon(),
                    shipment.getCargoWeightTons()
            );
            shipment.setCalculatedCarbonEmission(emission);
            shipment.setCalculationTimestamp(LocalDateTime.now());
        }

        return shipmentRepository.save(shipment);
    }

    // batch computing
    public List<Shipment> calculateBatchEmissions(List<Shipment> shipments) {
        shipments.forEach(this::calculateShipmentEmission);
        return shipmentRepository.saveAll(shipments);
    }
}