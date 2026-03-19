package com.greenchain.backend.repository;

import com.greenchain.backend.model.TransportMode;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface TransportModeRepository extends JpaRepository<TransportMode, Long> {
    Optional<TransportMode> findByMode(TransportMode.ModeType mode);
}