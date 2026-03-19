package com.greenchain.backend.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class TransportMode {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private ModeType mode; // AIR, SEA, TRUCK, RAIL

    private String displayName;
    private Double emissionFactorPerKmPerTon;

    public enum ModeType {
        AIR, SEA, TRUCK, RAIL
    }
}