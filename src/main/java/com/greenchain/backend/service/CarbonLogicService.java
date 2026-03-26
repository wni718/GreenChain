package com.greenchain.backend.service;

import com.greenchain.backend.dto.CarbonCalculateResponse;
import com.greenchain.backend.dto.RecommendResponse;
import com.greenchain.backend.model.TransportMode;
import com.greenchain.backend.repository.TransportModeRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Comparator;
import java.util.List;

@Service
public class CarbonLogicService {

    private final TransportModeRepository transportModeRepository;

    public CarbonLogicService(TransportModeRepository transportModeRepository) {
        this.transportModeRepository = transportModeRepository;
    }

    public CarbonCalculateResponse calculate(Double distanceKm, Double cargoWeightTons, String mode) {
        if (distanceKm == null || cargoWeightTons == null || mode == null || mode.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "distance_km, cargo_weight_tons, mode are required");
        }

        TransportMode.ModeType modeType = parseMode(mode);
        TransportMode transportMode = transportModeRepository.findByMode(modeType)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "unknown mode: " + mode));

        Double factor = transportMode.getEmissionFactorPerKmPerTon();
        if (factor == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "emission factor missing for mode: " + mode);
        }

        double carbon = distanceKm * factor * cargoWeightTons;
        return new CarbonCalculateResponse(carbon, "kg CO2e");
    }

    public RecommendResponse recommendBestMode(String currentMode) {
        if (currentMode == null || currentMode.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "current_mode is required");
        }

        TransportMode.ModeType currentModeType = parseMode(currentMode);
        TransportMode current = transportModeRepository.findByMode(currentModeType)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "unknown current_mode: " + currentMode));

        Double currentFactor = current.getEmissionFactorPerKmPerTon();
        if (currentFactor == null || currentFactor <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "invalid emission factor for current_mode");
        }

        List<TransportMode> all = transportModeRepository.findAll();
        if (all.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "transport_mode table is empty");
        }

        TransportMode best = all.stream()
                .filter(m -> m.getEmissionFactorPerKmPerTon() != null && m.getEmissionFactorPerKmPerTon() > 0)
                .min(Comparator.comparing(TransportMode::getEmissionFactorPerKmPerTon))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "no valid emission factors found"));

        Double bestFactor = best.getEmissionFactorPerKmPerTon();
        double savingPercent = (currentFactor - bestFactor) / currentFactor * 100.0;
        if (savingPercent < 0) savingPercent = 0; // Prevent floating point/data exceptions from causing negative numbers

        // Return a string in the form of "30%" as required
        String saving = Math.round(savingPercent) + "%";

        return new RecommendResponse(best.getMode().name().toLowerCase(), saving);
    }

    private TransportMode.ModeType parseMode(String mode) {
        try {
            return TransportMode.ModeType.valueOf(mode.trim().toUpperCase());
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "invalid mode: " + mode);
        }
    }
}

