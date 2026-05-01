package com.greenchain.backend.repository;

import com.greenchain.backend.model.Shipment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.util.Map;

public interface ShipmentRepository extends JpaRepository<Shipment, Long> {
    List<Shipment> findBySupplierId(Long supplierId);

    List<Shipment> findBySupplierUserUsername(String username);

    List<Shipment> findByTransportModeId(Long transportModeId);

    @Query("SELECT SUM(s.calculatedCarbonEmission) FROM Shipment s")
    Double sumAllCarbonEmissions();

    @Query("SELECT COUNT(DISTINCT s.supplier.id) FROM Shipment s WHERE s.supplier IS NOT NULL")
    Long countDistinctSuppliersInShipments();

    // Pagination support
    Page<Shipment> findAll(Pageable pageable);

    Page<Shipment> findBySupplierUserUsername(String username, Pageable pageable);
}