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
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "distance_km, cargo_weight_tons, mode are required");
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

    public RecommendResponse recommendBestMode(String currentMode, Double distanceKm, Double cargoWeightTons) {
        if (currentMode == null || currentMode.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "current_mode is required");
        }

        TransportMode.ModeType currentModeType = parseMode(currentMode);
        TransportMode current = transportModeRepository.findByMode(currentModeType)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "unknown current_mode: " + currentMode));

        Double currentFactor = current.getEmissionFactorPerKmPerTon();
        if (currentFactor == null || currentFactor <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "invalid emission factor for current_mode");
        }

        List<TransportMode> all = transportModeRepository.findAll();
        if (all.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "transport_mode table is empty");
        }

        // Filter valid transport modes
        List<TransportMode> validModes = all.stream()
                .filter(m -> m.getEmissionFactorPerKmPerTon() != null && m.getEmissionFactorPerKmPerTon() > 0)
                .collect(java.util.stream.Collectors.toList());

        if (validModes.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "no valid emission factors found");
        }

        // Smart recommendation based on distance and weight
        TransportMode best = recommendBestModeBasedOnContext(validModes, distanceKm, cargoWeightTons);

        Double bestFactor = best.getEmissionFactorPerKmPerTon();

        // Calculate emissions based on actual distance and weight
        double currentEmission = 0;
        double recommendedEmission = 0;
        if (distanceKm != null && cargoWeightTons != null) {
            currentEmission = currentFactor * distanceKm * cargoWeightTons;
            recommendedEmission = bestFactor * distanceKm * cargoWeightTons;
        } else {
            // Fallback to factor-based calculation if no distance/weight provided
            currentEmission = currentFactor;
            recommendedEmission = bestFactor;
        }

        // Calculate saving percentage and amount
        double savingPercent = 0;
        double savingAmount = 0;
        if (currentEmission > 0) {
            savingAmount = currentEmission - recommendedEmission;
            savingPercent = (savingAmount / currentEmission) * 100.0;
            if (savingPercent < 0)
                savingPercent = 0; // Prevent negative savings
        }

        // Return a string in the form of "30%" as required
        String saving = Math.round(savingPercent) + "%";

        // Calculate time and cost factors (relative to current mode)
        double timeFactor = calculateTimeFactor(current.getMode(), best.getMode(), distanceKm);
        double costFactor = calculateCostFactor(current.getMode(), best.getMode(), distanceKm, cargoWeightTons);

        return new RecommendResponse(
                best.getMode().name().toLowerCase(),
                saving,
                savingAmount,
                "kg CO2e",
                currentEmission,
                recommendedEmission,
                timeFactor,
                costFactor);
    }

    private double calculateTimeFactor(TransportMode.ModeType currentMode, TransportMode.ModeType recommendedMode,
            Double distanceKm) {
        // Relative time factors (1.0 = same as current, <1.0 = faster, >1.0 = slower)
        java.util.Map<TransportMode.ModeType, Double> speedFactors = new java.util.HashMap<>();
        speedFactors.put(TransportMode.ModeType.AIR, 1.0); // Fastest
        speedFactors.put(TransportMode.ModeType.TRUCK, 1.5); // Faster than rail/sea
        speedFactors.put(TransportMode.ModeType.RAIL, 2.5); // Slower than truck
        speedFactors.put(TransportMode.ModeType.SEA, 5.0); // Slowest

        double currentSpeed = speedFactors.get(currentMode);
        double recommendedSpeed = speedFactors.get(recommendedMode);

        // Calculate relative time factor
        return recommendedSpeed / currentSpeed;
    }

    private double calculateCostFactor(TransportMode.ModeType currentMode, TransportMode.ModeType recommendedMode,
            Double distanceKm, Double cargoWeightTons) {
        // Relative cost factors (1.0 = same as current, <1.0 = cheaper, >1.0 = more
        // expensive)
        java.util.Map<TransportMode.ModeType, Double> costFactors = new java.util.HashMap<>();
        costFactors.put(TransportMode.ModeType.AIR, 5.0); // Most expensive
        costFactors.put(TransportMode.ModeType.TRUCK, 2.0); // More expensive than rail/sea
        costFactors.put(TransportMode.ModeType.RAIL, 1.5); // Cheaper than truck
        costFactors.put(TransportMode.ModeType.SEA, 1.0); // Cheapest

        double currentCost = costFactors.get(currentMode);
        double recommendedCost = costFactors.get(recommendedMode);

        // Calculate relative cost factor
        return recommendedCost / currentCost;
    }

    private TransportMode recommendBestModeBasedOnContext(List<TransportMode> modes, Double distanceKm,
            Double cargoWeightTons) {
        // Default to the mode with lowest emission factor if no context provided
        if (distanceKm == null || cargoWeightTons == null) {
            return modes.stream()
                    .min(Comparator.comparing(TransportMode::getEmissionFactorPerKmPerTon))
                    .orElseThrow(() -> new RuntimeException("No valid transport modes"));
        }

        // Define mode preferences based on distance and weight
        // For short distances (< 500km), truck is more practical
        // For medium distances (500-5000km), rail is better
        // For long distances (> 5000km), sea is best
        // For very light cargo (< 1 ton), air might be considered for speed

        // Calculate scores for each mode
        java.util.Map<TransportMode, Double> scores = new java.util.HashMap<>();

        for (TransportMode mode : modes) {
            double score = calculateModeScore(mode, distanceKm, cargoWeightTons);
            scores.put(mode, score);
        }

        // Return the mode with the lowest score (better)
        return scores.entrySet().stream()
                .min(java.util.Map.Entry.comparingByValue())
                .map(java.util.Map.Entry::getKey)
                .orElseThrow(() -> new RuntimeException("No valid transport modes"));
    }

    private double calculateModeScore(TransportMode mode, double distanceKm, double cargoWeightTons) {
        double emissionFactor = mode.getEmissionFactorPerKmPerTon();
        double score = emissionFactor;

        // Adjust score based on distance and mode suitability
        switch (mode.getMode()) {
            case AIR:
                // Air is fast but expensive and high emission
                // Only consider for very light cargo and long distances
                if (cargoWeightTons > 5) {
                    score *= 10; // Heavy penalty for heavy cargo
                } else if (distanceKm < 1000) {
                    score *= 5; // Heavy penalty for short distances
                }
                break;
            case SEA:
                // Sea is cheapest and lowest emission but slow
                // Best for long distances and heavy cargo
                if (distanceKm < 500) {
                    score *= 10; // Heavy penalty for very short distances
                } else if (distanceKm < 2000) {
                    score *= 5; // Penalty for medium distances
                } else if (cargoWeightTons < 1) {
                    score *= 8; // Heavy penalty for very light cargo
                }
                break;
            case TRUCK:
                // Truck is flexible but higher emission
                // Best for short to medium distances
                if (distanceKm > 3000) {
                    score *= 8; // Heavy penalty for very long distances
                } else if (distanceKm > 1000) {
                    score *= 3; // Penalty for long distances
                }
                break;
            case RAIL:
                // Rail is efficient for medium to long distances
                // Good for heavy cargo
                if (distanceKm < 300) {
                    score *= 5; // Heavy penalty for very short distances
                } else if (distanceKm < 800) {
                    score *= 2; // Penalty for short distances
                }
                break;
        }

        return score;
    }

    private TransportMode.ModeType parseMode(String mode) {
        try {
            return TransportMode.ModeType.valueOf(mode.trim().toUpperCase());
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "invalid mode: " + mode);
        }
    }
}
