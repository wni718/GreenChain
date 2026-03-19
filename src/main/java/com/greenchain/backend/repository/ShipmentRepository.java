package com.greenchain.backend.repository;

import com.greenchain.backend.model.Shipment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface ShipmentRepository extends JpaRepository<Shipment, Long> {
    List<Shipment> findBySupplierId(Long supplierId);

    @Query("SELECT SUM(s.calculatedCarbonEmission) FROM Shipment s")
    Double sumAllCarbonEmissions();
}