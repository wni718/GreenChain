package com.greenchain.backend.service;

import com.greenchain.backend.dto.CarbonCalculateResponse;
import com.greenchain.backend.dto.HistoryAnalysisResponse;
import com.greenchain.backend.dto.RecommendResponse;
import com.greenchain.backend.model.Shipment;
import com.greenchain.backend.model.TransportMode;
import com.greenchain.backend.repository.ShipmentRepository;
import com.greenchain.backend.repository.TransportModeRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Comparator;
import java.util.List;

@Service
public class CarbonLogicService {

    private final TransportModeRepository transportModeRepository;
    private final ShipmentRepository shipmentRepository;

    public CarbonLogicService(TransportModeRepository transportModeRepository, ShipmentRepository shipmentRepository) {
        this.transportModeRepository = transportModeRepository;
        this.shipmentRepository = shipmentRepository;
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

    public HistoryAnalysisResponse analyzeHistoryData() {
        List<Shipment> allShipments = shipmentRepository.findAll();

        if (allShipments.isEmpty()) {
            return new HistoryAnalysisResponse(
                    0, 0.0, 0.0, "N/A", "N/A", 0.0, 0.0,
                    "No historical data available. Start recording shipments to get insights.");
        }

        int totalShipments = allShipments.size();

        // Calculate total carbon emission
        double totalEmission = allShipments.stream()
                .filter(s -> s.getCalculatedCarbonEmission() != null)
                .mapToDouble(Shipment::getCalculatedCarbonEmission)
                .sum();

        // Calculate average emission per shipment
        double avgEmission = totalEmission / totalShipments;

        // Find most used transport mode
        java.util.Map<String, Long> modeUsageCount = new java.util.HashMap<>();
        for (Shipment s : allShipments) {
            if (s.getTransportMode() != null && s.getTransportMode().getMode() != null) {
                String modeName = s.getTransportMode().getMode().name();
                modeUsageCount.merge(modeName, 1L, Long::sum);
            }
        }
        String mostUsedMode = modeUsageCount.entrySet().stream()
                .max(java.util.Map.Entry.comparingByValue())
                .map(java.util.Map.Entry::getKey)
                .orElse("N/A");

        // Find lowest carbon transport mode (based on historical average)
        java.util.Map<String, Double> modeEmissionSum = new java.util.HashMap<>();
        java.util.Map<String, Long> modeCount = new java.util.HashMap<>();
        for (Shipment s : allShipments) {
            if (s.getTransportMode() != null && s.getTransportMode().getMode() != null
                    && s.getCalculatedCarbonEmission() != null) {
                String modeName = s.getTransportMode().getMode().name();
                modeEmissionSum.merge(modeName, s.getCalculatedCarbonEmission(), Double::sum);
                modeCount.merge(modeName, 1L, Long::sum);
            }
        }

        String lowestCarbonMode = modeEmissionSum.entrySet().stream()
                .min((e1, e2) -> {
                    double avg1 = e1.getValue() / modeCount.get(e1.getKey());
                    double avg2 = e2.getValue() / modeCount.get(e2.getKey());
                    return Double.compare(avg1, avg2);
                })
                .map(java.util.Map.Entry::getKey)
                .orElse("N/A");

        // Calculate potential savings if user switched to lowest carbon mode
        List<TransportMode> allModes = transportModeRepository.findAll();
        TransportMode lowestFactorMode = allModes.stream()
                .filter(m -> m.getEmissionFactorPerKmPerTon() != null && m.getEmissionFactorPerKmPerTon() > 0)
                .min(Comparator.comparing(TransportMode::getEmissionFactorPerKmPerTon))
                .orElse(null);

        double potentialSavingsPercent = 0.0;
        double potentialSavingsAmount = 0.0;
        String recommendation = "";

        if (lowestFactorMode != null && !"N/A".equals(mostUsedMode) && !mostUsedMode.equals(lowestCarbonMode)) {
            TransportMode mostUsedTransportMode = allModes.stream()
                    .filter(m -> m.getMode() != null && m.getMode().name().equals(mostUsedMode))
                    .findFirst()
                    .orElse(null);

            if (mostUsedTransportMode != null) {
                double currentFactor = mostUsedTransportMode.getEmissionFactorPerKmPerTon();
                double bestFactor = lowestFactorMode.getEmissionFactorPerKmPerTon();
                if (currentFactor > 0) {
                    potentialSavingsPercent = ((currentFactor - bestFactor) / currentFactor) * 100.0;
                    potentialSavingsAmount = totalEmission * (potentialSavingsPercent / 100.0);
                }

                recommendation = String.format(
                        "Based on your history, switching from %s to %s for your shipments could reduce carbon emissions by %.1f%% (%.2f kg CO2e). Consider using %s for long-distance heavy cargo shipments.",
                        mostUsedMode, lowestCarbonMode, potentialSavingsPercent, potentialSavingsAmount,
                        lowestCarbonMode);
            }
        } else {
            recommendation = "Great job! You're already using the most eco-friendly transport mode for your shipments.";
        }

        return new HistoryAnalysisResponse(
                totalShipments,
                totalEmission,
                avgEmission,
                mostUsedMode,
                lowestCarbonMode,
                potentialSavingsPercent,
                potentialSavingsAmount,
                recommendation);
    }

    public RecommendResponse recommendBestModeFromHistory(String origin, String destination, Double cargoWeightTons) {
        // Find similar historical shipments based on origin/destination patterns
        List<Shipment> allShipments = shipmentRepository.findAll();

        if (allShipments.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "No historical data available for smart recommendation");
        }

        // Find shipments with similar origin or destination
        List<Shipment> similarShipments = allShipments.stream()
                .filter(s -> origin != null && destination != null)
                .filter(s -> (origin.equalsIgnoreCase(s.getOrigin()) || origin.equalsIgnoreCase(s.getDestination())) ||
                        (destination.equalsIgnoreCase(s.getOrigin())
                                || destination.equalsIgnoreCase(s.getDestination())))
                .collect(java.util.stream.Collectors.toList());

        // If no similar shipments found, use distance-based recommendation
        if (similarShipments.isEmpty()) {
            // Estimate distance if not provided
            Double estimatedDistance = estimateDistance(origin, destination);
            return recommendBestModeFromDistanceAndWeight(estimatedDistance, cargoWeightTons);
        }

        // Find best performing mode from similar shipments
        java.util.Map<String, Double> modeEmissionSum = new java.util.HashMap<>();
        java.util.Map<String, Long> modeCount = new java.util.HashMap<>();

        for (Shipment s : similarShipments) {
            if (s.getTransportMode() != null && s.getTransportMode().getMode() != null
                    && s.getCalculatedCarbonEmission() != null) {
                String modeName = s.getTransportMode().getMode().name();
                modeEmissionSum.merge(modeName, s.getCalculatedCarbonEmission(), Double::sum);
                modeCount.merge(modeName, 1L, Long::sum);
            }
        }

        // Find the mode with lowest average emission
        String bestHistoricalMode = modeEmissionSum.entrySet().stream()
                .min((e1, e2) -> {
                    double avg1 = e1.getValue() / modeCount.get(e1.getKey());
                    double avg2 = e2.getValue() / modeCount.get(e2.getKey());
                    return Double.compare(avg1, avg2);
                })
                .map(java.util.Map.Entry::getKey)
                .orElse(null);

        if (bestHistoricalMode == null) {
            Double estimatedDistance = estimateDistance(origin, destination);
            return recommendBestModeFromDistanceAndWeight(estimatedDistance, cargoWeightTons);
        }

        // Calculate emissions and savings
        TransportMode.ModeType bestModeType = parseMode(bestHistoricalMode);
        TransportMode bestMode = transportModeRepository.findByMode(bestModeType)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "unknown mode: " + bestHistoricalMode));

        Double estimatedDistance = estimateDistance(origin, destination);
        double emission = 0;
        if (estimatedDistance != null && cargoWeightTons != null) {
            emission = bestMode.getEmissionFactorPerKmPerTon() * estimatedDistance * cargoWeightTons;
        }

        return new RecommendResponse(
                bestHistoricalMode.toLowerCase(),
                "Based on similar routes",
                emission,
                "kg CO2e",
                emission,
                emission,
                1.0,
                1.0);
    }

    private Double estimateDistance(String origin, String destination) {
        // Simple estimation based on string length difference as a proxy
        // In a real application, this would use a geocoding API or distance matrix API
        if (origin == null || destination == null) {
            return null;
        }
        int lengthDiff = Math.abs(origin.length() - destination.length());
        // Return a default medium distance estimate
        return 1000.0 + (lengthDiff * 100.0);
    }

    private RecommendResponse recommendBestModeFromDistanceAndWeight(Double distanceKm, Double cargoWeightTons) {
        List<TransportMode> all = transportModeRepository.findAll();
        List<TransportMode> validModes = all.stream()
                .filter(m -> m.getEmissionFactorPerKmPerTon() != null && m.getEmissionFactorPerKmPerTon() > 0)
                .collect(java.util.stream.Collectors.toList());

        TransportMode best = recommendBestModeBasedOnContext(validModes, distanceKm, cargoWeightTons);

        double emission = 0;
        if (distanceKm != null && cargoWeightTons != null) {
            emission = best.getEmissionFactorPerKmPerTon() * distanceKm * cargoWeightTons;
        }

        return new RecommendResponse(
                best.getMode().name().toLowerCase(),
                "Based on distance & weight",
                emission,
                "kg CO2e",
                emission,
                emission,
                1.0,
                1.0);
    }

    private TransportMode.ModeType parseMode(String mode) {
        try {
            return TransportMode.ModeType.valueOf(mode.trim().toUpperCase());
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "invalid mode: " + mode);
        }
    }
}
