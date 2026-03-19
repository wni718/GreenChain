package com.greenchain.backend.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;  // 添加这个 import

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

    // 添加这个字段
    private LocalDateTime calculationTimestamp;
}