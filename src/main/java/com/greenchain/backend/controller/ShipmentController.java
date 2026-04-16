package com.greenchain.backend.controller;

import com.greenchain.backend.dto.ShipmentDTO;
import com.greenchain.backend.model.Shipment;
import com.greenchain.backend.model.Supplier;
import com.greenchain.backend.model.TransportMode;
import com.greenchain.backend.repository.ShipmentRepository;
import com.greenchain.backend.service.CarbonCalculationService;
import com.greenchain.backend.service.GeoLocationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/shipments")
public class ShipmentController {

    @Autowired
    private ShipmentRepository shipmentRepository;

    @Autowired
    private CarbonCalculationService carbonService;

    @Autowired
    private GeoLocationService geoLocationService;

    @GetMapping
    public List<Shipment> getAllShipments() {
        return shipmentRepository.findAll();
    }

    @GetMapping("/with-coordinates")
    public List<ShipmentDTO> getAllShipmentsWithCoordinates() {
        List<Shipment> shipments = shipmentRepository.findAll();
        return shipments.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    private ShipmentDTO convertToDTO(Shipment shipment) {
        ShipmentDTO dto = new ShipmentDTO();
        dto.setId(shipment.getId());

        if (shipment.getSupplier() != null) {
            dto.setSupplierId(shipment.getSupplier().getId());
            dto.setSupplierName(shipment.getSupplier().getName());
        }

        dto.setOrigin(shipment.getOrigin());
        dto.setDestination(shipment.getDestination());

        double[] originCoords = geoLocationService.getCoordinates(shipment.getOrigin());
        dto.setOriginLat(originCoords[0]);
        dto.setOriginLng(originCoords[1]);

        double[] destCoords = geoLocationService.getCoordinates(shipment.getDestination());
        dto.setDestLat(destCoords[0]);
        dto.setDestLng(destCoords[1]);

        dto.setDistanceKm(shipment.getDistanceKm());
        dto.setCargoWeightTons(shipment.getCargoWeightTons());
        dto.setShipmentDate(shipment.getShipmentDate());
        dto.setCalculatedCarbonEmission(shipment.getCalculatedCarbonEmission());
        dto.setCalculationTimestamp(shipment.getCalculationTimestamp());

        if (shipment.getTransportMode() != null) {
            dto.setTransportMode(shipment.getTransportMode().getMode().name());
            dto.setTransportModeName(shipment.getTransportMode().getDisplayName());
        }

        return dto;
    }

    @PostMapping
    public Shipment createShipment(@RequestBody Shipment shipment) {
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