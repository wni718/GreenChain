package com.greenchain.backend.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class ShipmentDTO {
    private Long id;
    private Long supplierId;
    private String supplierName;
    private String origin;
    private String destination;
    private Double originLat;
    private Double originLng;
    private Double destLat;
    private Double destLng;
    private Double distanceKm;
    private Double cargoWeightTons;
    private LocalDate shipmentDate;
    private Double calculatedCarbonEmission;
    private LocalDateTime calculationTimestamp;
    private String transportMode;
    private String transportModeName;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getSupplierId() {
        return supplierId;
    }

    public void setSupplierId(Long supplierId) {
        this.supplierId = supplierId;
    }

    public String getSupplierName() {
        return supplierName;
    }

    public void setSupplierName(String supplierName) {
        this.supplierName = supplierName;
    }

    public String getOrigin() {
        return origin;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public Double getOriginLat() {
        return originLat;
    }

    public void setOriginLat(Double originLat) {
        this.originLat = originLat;
    }

    public Double getOriginLng() {
        return originLng;
    }

    public void setOriginLng(Double originLng) {
        this.originLng = originLng;
    }

    public Double getDestLat() {
        return destLat;
    }

    public void setDestLat(Double destLat) {
        this.destLat = destLat;
    }

    public Double getDestLng() {
        return destLng;
    }

    public void setDestLng(Double destLng) {
        this.destLng = destLng;
    }

    public Double getDistanceKm() {
        return distanceKm;
    }

    public void setDistanceKm(Double distanceKm) {
        this.distanceKm = distanceKm;
    }

    public Double getCargoWeightTons() {
        return cargoWeightTons;
    }

    public void setCargoWeightTons(Double cargoWeightTons) {
        this.cargoWeightTons = cargoWeightTons;
    }

    public LocalDate getShipmentDate() {
        return shipmentDate;
    }

    public void setShipmentDate(LocalDate shipmentDate) {
        this.shipmentDate = shipmentDate;
    }

    public Double getCalculatedCarbonEmission() {
        return calculatedCarbonEmission;
    }

    public void setCalculatedCarbonEmission(Double calculatedCarbonEmission) {
        this.calculatedCarbonEmission = calculatedCarbonEmission;
    }

    public LocalDateTime getCalculationTimestamp() {
        return calculationTimestamp;
    }

    public void setCalculationTimestamp(LocalDateTime calculationTimestamp) {
        this.calculationTimestamp = calculationTimestamp;
    }

    public String getTransportMode() {
        return transportMode;
    }

    public void setTransportMode(String transportMode) {
        this.transportMode = transportMode;
    }

    public String getTransportModeName() {
        return transportModeName;
    }

    public void setTransportModeName(String transportModeName) {
        this.transportModeName = transportModeName;
    }
}