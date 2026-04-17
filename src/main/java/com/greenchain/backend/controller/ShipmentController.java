package com.greenchain.backend.controller;

import com.greenchain.backend.dto.ShipmentDTO;
import com.greenchain.backend.model.Shipment;
import com.greenchain.backend.model.Supplier;
import com.greenchain.backend.model.TransportMode;
import com.greenchain.backend.model.User;
import com.greenchain.backend.repository.ShipmentRepository;
import com.greenchain.backend.repository.SupplierRepository;
import com.greenchain.backend.repository.UserRepository;
import com.greenchain.backend.service.CarbonCalculationService;
import com.greenchain.backend.service.GeoLocationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/shipments")
public class ShipmentController {

    @Autowired
    private ShipmentRepository shipmentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SupplierRepository supplierRepository;

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
    public ResponseEntity<Shipment> createShipment(@RequestBody Shipment shipment, Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        User currentUser = userRepository.findByUsername(principal.getName()).orElse(null);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // Only admin and supplier can create shipments
        if (currentUser.getRole() != User.Role.ADMIN && currentUser.getRole() != User.Role.SUPPLIER) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        // If user is supplier, they can only create shipments for their own supplier
        if (currentUser.getRole() == User.Role.SUPPLIER) {
            // Find the supplier associated with the current user
            Supplier supplier = supplierRepository.findByUserUsername(currentUser.getUsername()).orElse(null);
            if (supplier == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
            }

            // Set the supplier for the shipment to the user's own supplier
            shipment.setSupplier(supplier);
        }

        try {
            Shipment createdShipment = carbonService.calculateShipmentEmission(shipment);
            return ResponseEntity.ok(createdShipment);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
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
            // Use the existing supplier with full user association
            // Don't create a new supplier object, just keep the existing one
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
    public ResponseEntity<Shipment> updateShipmentPut(@PathVariable Long id, @RequestBody Shipment body,
            Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // Check permission
        if (!hasShipmentPermission(id, principal)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        try {
            Shipment updatedShipment = applyShipmentUpdate(id, body);
            return ResponseEntity.ok(updatedShipment);
        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode()).build();
        }
    }

    @PostMapping("/{id}/update")
    public ResponseEntity<Shipment> updateShipmentPost(@PathVariable Long id, @RequestBody Shipment body,
            Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // Check permission
        if (!hasShipmentPermission(id, principal)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        try {
            Shipment updatedShipment = applyShipmentUpdate(id, body);
            return ResponseEntity.ok(updatedShipment);
        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode()).build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteShipment(@PathVariable Long id, Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // Check permission
        if (!hasShipmentPermission(id, principal)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        if (!shipmentRepository.existsById(id)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        shipmentRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    private boolean hasShipmentPermission(Long shipmentId, Principal principal) {
        User currentUser = userRepository.findByUsername(principal.getName()).orElse(null);
        if (currentUser == null) {
            return false;
        }

        // Admin can do anything
        if (currentUser.getRole() == User.Role.ADMIN) {
            return true;
        }

        // Supplier can only modify shipments belonging to their own supplier
        if (currentUser.getRole() == User.Role.SUPPLIER) {
            // Find the supplier associated with the current user
            Supplier supplier = supplierRepository.findByUserUsername(currentUser.getUsername()).orElse(null);
            if (supplier == null) {
                return false;
            }

            // Check if the shipment belongs to this supplier
            Shipment shipment = shipmentRepository.findById(shipmentId).orElse(null);
            return shipment != null && shipment.getSupplier() != null &&
                    shipment.getSupplier().getId().equals(supplier.getId());
        }

        return false;
    }

    @GetMapping("/my")
    public ResponseEntity<List<Shipment>> getMyShipments(Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        String username = principal.getName();
        List<Shipment> shipments = shipmentRepository.findBySupplierUserUsername(username);
        return ResponseEntity.ok(shipments);
    }

    @GetMapping("/test-permission/{id}")
    public ResponseEntity<?> testPermission(@PathVariable Long id, Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        boolean hasPermission = hasShipmentPermission(id, principal);
        User currentUser = userRepository.findByUsername(principal.getName()).orElse(null);
        Shipment shipment = shipmentRepository.findById(id).orElse(null);

        // Add debug info for supplier lookup
        Supplier supplierByUser = supplierRepository.findByUserUsername(currentUser.getUsername()).orElse(null);

        String result = "Permission: " + hasPermission + "\n";
        result += "Current user: " + currentUser + "\n";
        result += "Supplier by user: " + supplierByUser + "\n";
        result += "Shipment: " + shipment + "\n";
        if (shipment != null && shipment.getSupplier() != null) {
            result += "Shipment supplier: " + shipment.getSupplier() + "\n";
            result += "Shipment supplier user: " + shipment.getSupplier().getUser() + "\n";
            if (supplierByUser != null) {
                result += "Shipment supplier ID: " + shipment.getSupplier().getId() + "\n";
                result += "User's supplier ID: " + supplierByUser.getId() + "\n";
                result += "IDs match: " + shipment.getSupplier().getId().equals(supplierByUser.getId()) + "\n";
            }
        }

        return ResponseEntity.ok(result);
    }
}