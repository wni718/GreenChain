package com.greenchain.backend.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class Supplier {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String country;
    private Boolean hasEnvironmentalCertification;
    private Double emissionFactorPerUnit;
    private String contactEmail;
}