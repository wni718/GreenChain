package com.greenchain.backend.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Data
public class Shipment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Supplier supplier;

    @ManyToOne
    private TransportMode transportMode;

    private String origin;
    private String destination;
    private Double distanceKm;
    private Double cargoWeightTons;
    private LocalDate shipmentDate;
    private Double calculatedCarbonEmission;
    private LocalDateTime calculationTimestamp;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Supplier getSupplier() { return supplier; }
    public void setSupplier(Supplier supplier) { this.supplier = supplier; }
    public TransportMode getTransportMode() { return transportMode; }
    public void setTransportMode(TransportMode transportMode) { this.transportMode = transportMode; }
    public String getOrigin() { return origin; }
    public void setOrigin(String origin) { this.origin = origin; }
    public String getDestination() { return destination; }
    public void setDestination(String destination) { this.destination = destination; }
    public Double getDistanceKm() { return distanceKm; }
    public void setDistanceKm(Double distanceKm) { this.distanceKm = distanceKm; }
    public Double getCargoWeightTons() { return cargoWeightTons; }
    public void setCargoWeightTons(Double cargoWeightTons) { this.cargoWeightTons = cargoWeightTons; }
    public LocalDate getShipmentDate() { return shipmentDate; }
    public void setShipmentDate(LocalDate shipmentDate) { this.shipmentDate = shipmentDate; }
    public Double getCalculatedCarbonEmission() { return calculatedCarbonEmission; }
    public void setCalculatedCarbonEmission(Double calculatedCarbonEmission) { this.calculatedCarbonEmission = calculatedCarbonEmission; }
    public LocalDateTime getCalculationTimestamp() { return calculationTimestamp; }
    public void setCalculationTimestamp(LocalDateTime calculationTimestamp) { this.calculationTimestamp = calculationTimestamp; }
}