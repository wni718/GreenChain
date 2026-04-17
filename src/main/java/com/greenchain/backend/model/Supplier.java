package com.greenchain.backend.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class Supplier {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id")
    @JsonIgnore
    private User user;

    private String name;
    private String country;
    private Boolean hasEnvironmentalCertification;
    private Double emissionFactorPerUnit;
    private String contactEmail;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public Boolean getHasEnvironmentalCertification() {
        return hasEnvironmentalCertification;
    }

    public void setHasEnvironmentalCertification(Boolean hasEnvironmentalCertification) {
        this.hasEnvironmentalCertification = hasEnvironmentalCertification;
    }

    public Double getEmissionFactorPerUnit() {
        return emissionFactorPerUnit;
    }

    public void setEmissionFactorPerUnit(Double emissionFactorPerUnit) {
        this.emissionFactorPerUnit = emissionFactorPerUnit;
    }

    public String getContactEmail() {
        return contactEmail;
    }

    public void setContactEmail(String contactEmail) {
        this.contactEmail = contactEmail;
    }

    @Override
    public String toString() {
        return "Supplier{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", country='" + country + '\'' +
                ", hasEnvironmentalCertification=" + hasEnvironmentalCertification +
                ", emissionFactorPerUnit=" + emissionFactorPerUnit +
                ", contactEmail='" + contactEmail + '\'' +
                '}';
    }
}