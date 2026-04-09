package com.greenchain.backend.controller;

import com.greenchain.backend.model.Shipment;
import com.greenchain.backend.model.Supplier;
import com.greenchain.backend.model.TransportMode;
import com.greenchain.backend.repository.ShipmentRepository;
import com.greenchain.backend.service.CarbonCalculationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

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

    @GetMapping("/{id}")
    public Shipment getShipmentById(@PathVariable Long id) {
        return shipmentRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    /**
     * Apply JSON body to an existing shipment and recalculate emissions.
     */
    private Shipment applyShipmentUpdate(Long id, Shipment body) {
        Shipment existing = shipmentRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        if (body.getSupplier() != null && body.getSupplier().getId() != null) {
            Supplier s = new Supplier();
            s.setId(body.getSupplier().getId());
            existing.setSupplier(s);
        }
        if (body.getTransportMode() != null && body.getTransportMode().getId() != null) {
            TransportMode m = new TransportMode();
            m.setId(body.getTransportMode().getId());
            existing.setTransportMode(m);
        }
        existing.setOrigin(body.getOrigin());
        existing.setDestination(body.getDestination());
        existing.setDistanceKm(body.getDistanceKm());
        existing.setCargoWeightTons(body.getCargoWeightTons());
        existing.setShipmentDate(body.getShipmentDate());

        return carbonService.calculateShipmentEmission(existing);
    }

    @PutMapping("/{id}")
    public Shipment updateShipmentPut(@PathVariable Long id, @RequestBody Shipment body) {
        return applyShipmentUpdate(id, body);
    }

    /**
     * Same as PUT but POST — avoids environments where PUT is stripped or mishandled (often surfaces as
     * "No static resource api/shipments/{id}" when the request never reaches the controller).
     */
    @PostMapping("/{id}/update")
    public Shipment updateShipmentPost(@PathVariable Long id, @RequestBody Shipment body) {
        return applyShipmentUpdate(id, body);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteShipment(@PathVariable Long id) {
        if (!shipmentRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        shipmentRepository.deleteById(id);
    }
}