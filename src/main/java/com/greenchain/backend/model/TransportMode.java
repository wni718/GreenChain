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
    private ModeType mode;

    private String displayName;
    private Double emissionFactorPerKmPerTon;

    public enum ModeType {
        AIR, SEA, TRUCK, RAIL
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public ModeType getMode() { return mode; }
    public void setMode(ModeType mode) { this.mode = mode; }
    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }
    public Double getEmissionFactorPerKmPerTon() { return emissionFactorPerKmPerTon; }
    public void setEmissionFactorPerKmPerTon(Double emissionFactorPerKmPerTon) { this.emissionFactorPerKmPerTon = emissionFactorPerKmPerTon; }
}