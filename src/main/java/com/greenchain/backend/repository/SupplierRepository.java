package com.greenchain.backend.repository;

import com.greenchain.backend.model.Supplier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SupplierRepository extends JpaRepository<Supplier, Long> {
    List<Supplier> findByCountry(String country);

    List<Supplier> findByHasEnvironmentalCertificationTrue();

    java.util.Optional<Supplier> findByUserUsername(String username);

    long countByHasEnvironmentalCertificationTrue();

    // Pagination support
    Page<Supplier> findAll(Pageable pageable);

    Page<Supplier> findByHasEnvironmentalCertificationTrue(Pageable pageable);
}