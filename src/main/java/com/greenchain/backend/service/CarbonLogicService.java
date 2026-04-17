package com.greenchain.backend.service;

import com.greenchain.backend.dto.CarbonCalculateResponse;
import com.greenchain.backend.dto.HistoryAnalysisResponse;
import com.greenchain.backend.dto.RecommendResponse;
import com.greenchain.backend.model.Shipment;
import com.greenchain.backend.model.Supplier;
import com.greenchain.backend.model.TransportMode;
import com.greenchain.backend.repository.ShipmentRepository;
import com.greenchain.backend.repository.SupplierRepository;
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
    private final SupplierRepository supplierRepository;

    public CarbonLogicService(TransportModeRepository transportModeRepository,
            ShipmentRepository shipmentRepository,
            SupplierRepository supplierRepository) {
        this.transportModeRepository = transportModeRepository;
        this.shipmentRepository = shipmentRepository;
        this.supplierRepository = supplierRepository;
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

    public RecommendResponse recommendBestMode(String currentMode, Double distanceKm, Double cargoWeightTons,
            Long supplierId) {
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

        Supplier supplier = null;
        Double supplierFactor = null;
        if (supplierId != null) {
            supplier = supplierRepository.findById(supplierId).orElse(null);
            if (supplier != null) {
                supplierFactor = supplier.getEmissionFactorPerUnit();
            }
        }

        List<TransportMode> all = transportModeRepository.findAll();
        if (all.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "transport_mode table is empty");
        }

        List<TransportMode> validModes = all.stream()
                .filter(m -> m.getEmissionFactorPerKmPerTon() != null && m.getEmissionFactorPerKmPerTon() > 0)
                .collect(java.util.stream.Collectors.toList());

        if (validModes.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "no valid emission factors found");
        }

        TransportMode best = recommendBestModeBasedOnContext(validModes, distanceKm, cargoWeightTons, supplierFactor);

        Double bestFactor = best.getEmissionFactorPerKmPerTon();

        double currentEmission = 0;
        double recommendedEmission = 0;
        if (distanceKm != null && cargoWeightTons != null) {
            currentEmission = calculateTotalEmission(distanceKm, cargoWeightTons, currentFactor, supplierFactor);
            recommendedEmission = calculateTotalEmission(distanceKm, cargoWeightTons, bestFactor, supplierFactor);
        } else {
            currentEmission = currentFactor;
            recommendedEmission = bestFactor;
        }

        double savingPercent = 0;
        double savingAmount = 0;
        if (currentEmission > 0) {
            savingAmount = currentEmission - recommendedEmission;
            savingPercent = (savingAmount / currentEmission) * 100.0;
            if (savingPercent < 0)
                savingPercent = 0;
        }

        String saving = Math.round(savingPercent) + "%";

        double timeFactor = calculateTimeFactor(current.getMode(), best.getMode(), distanceKm);
        double costFactor = calculateCostFactor(current.getMode(), best.getMode(), distanceKm, cargoWeightTons);

        String supplierInfo = supplier != null ? supplier.getName() : "Default";

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

    private double calculateTotalEmission(Double distanceKm, Double cargoWeightTons, Double transportFactor,
            Double supplierFactor) {
        double transportEmission = distanceKm * transportFactor * cargoWeightTons;
        double productionEmission = 0;
        if (supplierFactor != null && supplierFactor > 0) {
            productionEmission = cargoWeightTons * supplierFactor;
        }
        return transportEmission + productionEmission;
    }

    private double calculateTimeFactor(TransportMode.ModeType currentMode, TransportMode.ModeType recommendedMode,
            Double distanceKm) {
        java.util.Map<TransportMode.ModeType, Double> speedFactors = new java.util.HashMap<>();
        speedFactors.put(TransportMode.ModeType.AIR, 1.0);
        speedFactors.put(TransportMode.ModeType.TRUCK, 1.5);
        speedFactors.put(TransportMode.ModeType.RAIL, 2.5);
        speedFactors.put(TransportMode.ModeType.SEA, 5.0);

        double currentSpeed = speedFactors.get(currentMode);
        double recommendedSpeed = speedFactors.get(recommendedMode);

        return recommendedSpeed / currentSpeed;
    }

    private double calculateCostFactor(TransportMode.ModeType currentMode, TransportMode.ModeType recommendedMode,
            Double distanceKm, Double cargoWeightTons) {
        java.util.Map<TransportMode.ModeType, Double> costFactors = new java.util.HashMap<>();
        costFactors.put(TransportMode.ModeType.AIR, 5.0);
        costFactors.put(TransportMode.ModeType.TRUCK, 2.0);
        costFactors.put(TransportMode.ModeType.RAIL, 1.5);
        costFactors.put(TransportMode.ModeType.SEA, 1.0);

        double currentCost = costFactors.get(currentMode);
        double recommendedCost = costFactors.get(recommendedMode);

        return recommendedCost / currentCost;
    }

    private TransportMode recommendBestModeBasedOnContext(List<TransportMode> modes, Double distanceKm,
            Double cargoWeightTons, Double supplierFactor) {
        if (distanceKm == null || cargoWeightTons == null) {
            return modes.stream()
                    .min(Comparator.comparing(TransportMode::getEmissionFactorPerKmPerTon))
                    .orElseThrow(() -> new RuntimeException("No valid transport modes"));
        }

        java.util.Map<TransportMode, Double> scores = new java.util.HashMap<>();

        for (TransportMode mode : modes) {
            double score = calculateModeScoreWithSupplier(mode, distanceKm, cargoWeightTons, supplierFactor);
            scores.put(mode, score);
        }

        return scores.entrySet().stream()
                .min(java.util.Map.Entry.comparingByValue())
                .map(java.util.Map.Entry::getKey)
                .orElseThrow(() -> new RuntimeException("No valid transport modes"));
    }

    private double calculateModeScoreWithSupplier(TransportMode mode, double distanceKm, double cargoWeightTons,
            Double supplierFactor) {
        double emissionFactor = mode.getEmissionFactorPerKmPerTon();
        double score = emissionFactor;

        switch (mode.getMode()) {
            case AIR:
                if (cargoWeightTons > 5) {
                    score *= 10;
                } else if (distanceKm < 1000) {
                    score *= 5;
                }
                break;
            case SEA:
                if (distanceKm < 500) {
                    score *= 10;
                } else if (distanceKm < 2000) {
                    score *= 5;
                } else if (cargoWeightTons < 1) {
                    score *= 8;
                }
                break;
            case TRUCK:
                if (distanceKm > 3000) {
                    score *= 8;
                } else if (distanceKm > 1000) {
                    score *= 3;
                }
                break;
            case RAIL:
                if (distanceKm < 300) {
                    score *= 5;
                } else if (distanceKm < 800) {
                    score *= 2;
                }
                break;
        }

        if (supplierFactor != null && supplierFactor > 0) {
            double supplierContribution = supplierFactor / (emissionFactor * 100);
            score = score * (1 + supplierContribution);
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

        double totalEmission = 0;
        double totalProductionEmission = 0;
        for (Shipment s : allShipments) {
            if (s.getCalculatedCarbonEmission() != null) {
                totalEmission += s.getCalculatedCarbonEmission();
            }
            if (s.getSupplier() != null && s.getSupplier().getEmissionFactorPerUnit() != null
                    && s.getCargoWeightTons() != null) {
                totalProductionEmission += s.getCargoWeightTons() * s.getSupplier().getEmissionFactorPerUnit();
            }
        }

        double totalWithProduction = totalEmission + totalProductionEmission;
        double avgEmission = totalWithProduction / totalShipments;

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

        java.util.Map<String, Double> modeEmissionSum = new java.util.HashMap<>();
        java.util.Map<String, Long> modeCount = new java.util.HashMap<>();
        for (Shipment s : allShipments) {
            if (s.getTransportMode() != null && s.getTransportMode().getMode() != null
                    && s.getCalculatedCarbonEmission() != null) {
                String modeName = s.getTransportMode().getMode().name();
                double shipmentEmission = s.getCalculatedCarbonEmission();
                if (s.getSupplier() != null && s.getSupplier().getEmissionFactorPerUnit() != null
                        && s.getCargoWeightTons() != null) {
                    shipmentEmission += s.getCargoWeightTons() * s.getSupplier().getEmissionFactorPerUnit();
                }
                modeEmissionSum.merge(modeName, shipmentEmission, Double::sum);
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

        List<TransportMode> allModes = transportModeRepository.findAll();
        TransportMode lowestFactorMode = allModes.stream()
                .filter(m -> m.getEmissionFactorPerKmPerTon() != null && m.getEmissionFactorPerKmPerTon() > 0)
                .min(Comparator.comparing(TransportMode::getEmissionFactorPerKmPerTon))
                .orElse(null);

        double potentialSavingsPercent = 0.0;
        double potentialSavingsAmount = 0.0;
        String recommendation = "";

        if (lowestFactorMode != null && !"N/A".equals(mostUsedMode)) {
            // Calculate actual total emission with current transport modes
            double actualTotalEmission = totalWithProduction;
            
            // Calculate potential total emission using the lowest emission factor mode
            double potentialTotalEmission = 0;
            for (Shipment s : allShipments) {
                if (s.getDistanceKm() != null && s.getCargoWeightTons() != null) {
                    double transportEmission = s.getDistanceKm() * lowestFactorMode.getEmissionFactorPerKmPerTon() * s.getCargoWeightTons();
                    double productionEmission = 0;
                    if (s.getSupplier() != null && s.getSupplier().getEmissionFactorPerUnit() != null) {
                        productionEmission = s.getCargoWeightTons() * s.getSupplier().getEmissionFactorPerUnit();
                    }
                    potentialTotalEmission += transportEmission + productionEmission;
                }
            }

            if (actualTotalEmission > 0 && potentialTotalEmission < actualTotalEmission) {
                potentialSavingsAmount = actualTotalEmission - potentialTotalEmission;
                potentialSavingsPercent = (potentialSavingsAmount / actualTotalEmission) * 100.0;

                recommendation = String.format(
                        "Based on your history (including production emissions), switching to %s for your shipments could reduce carbon emissions by %.1f%% (%.2f kg CO2e). Consider using %s for more eco-friendly transportation.",
                        lowestFactorMode.getMode().name(), potentialSavingsPercent, potentialSavingsAmount,
                        lowestFactorMode.getMode().name());
            } else {
                recommendation = "Great job! You're already using transport modes with optimal carbon emissions (including production emissions).";
            }
        } else {
            recommendation = "No valid transport modes available for savings analysis.";
        }

        return new HistoryAnalysisResponse(
                totalShipments,
                totalWithProduction,
                avgEmission,
                mostUsedMode,
                lowestCarbonMode,
                potentialSavingsPercent,
                potentialSavingsAmount,
                recommendation);
    }

    public RecommendResponse recommendBestModeFromHistory(String origin, String destination, Double cargoWeightTons,
            Long supplierId) {
        List<Shipment> allShipments = shipmentRepository.findAll();

        if (allShipments.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "No historical data available for smart recommendation");
        }

        Supplier supplier = null;
        Double supplierFactor = null;
        if (supplierId != null) {
            supplier = supplierRepository.findById(supplierId).orElse(null);
            if (supplier != null) {
                supplierFactor = supplier.getEmissionFactorPerUnit();
            }
        }

        List<Shipment> similarShipments = allShipments.stream()
                .filter(s -> origin != null && destination != null)
                .filter(s -> (origin.equalsIgnoreCase(s.getOrigin()) || origin.equalsIgnoreCase(s.getDestination())) ||
                        (destination.equalsIgnoreCase(s.getOrigin())
                                || destination.equalsIgnoreCase(s.getDestination())))
                .collect(java.util.stream.Collectors.toList());

        if (similarShipments.isEmpty()) {
            Double estimatedDistance = estimateDistance(origin, destination);
            return recommendBestModeFromDistanceAndWeight(estimatedDistance, cargoWeightTons, supplierFactor);
        }

        java.util.Map<String, Double> modeEmissionSum = new java.util.HashMap<>();
        java.util.Map<String, Long> modeCount = new java.util.HashMap<>();

        for (Shipment s : similarShipments) {
            if (s.getTransportMode() != null && s.getTransportMode().getMode() != null
                    && s.getCalculatedCarbonEmission() != null) {
                String modeName = s.getTransportMode().getMode().name();
                double shipmentEmission = s.getCalculatedCarbonEmission();
                if (s.getSupplier() != null && s.getSupplier().getEmissionFactorPerUnit() != null
                        && s.getCargoWeightTons() != null) {
                    shipmentEmission += s.getCargoWeightTons() * s.getSupplier().getEmissionFactorPerUnit();
                }
                modeEmissionSum.merge(modeName, shipmentEmission, Double::sum);
                modeCount.merge(modeName, 1L, Long::sum);
            }
        }

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
            return recommendBestModeFromDistanceAndWeight(estimatedDistance, cargoWeightTons, supplierFactor);
        }

        TransportMode.ModeType bestModeType = parseMode(bestHistoricalMode);
        TransportMode bestMode = transportModeRepository.findByMode(bestModeType)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "unknown mode: " + bestHistoricalMode));

        Double estimatedDistance = estimateDistance(origin, destination);
        double emission = 0;
        if (estimatedDistance != null && cargoWeightTons != null) {
            emission = calculateTotalEmission(estimatedDistance, cargoWeightTons,
                    bestMode.getEmissionFactorPerKmPerTon(), supplierFactor);
        }

        return new RecommendResponse(
                bestHistoricalMode.toLowerCase(),
                "Based on similar routes (with production)",
                emission,
                "kg CO2e",
                emission,
                emission,
                1.0,
                1.0);
    }

    private Double estimateDistance(String origin, String destination) {
        if (origin == null || destination == null) {
            return null;
        }
        int lengthDiff = Math.abs(origin.length() - destination.length());
        return 1000.0 + (lengthDiff * 100.0);
    }

    private RecommendResponse recommendBestModeFromDistanceAndWeight(Double distanceKm, Double cargoWeightTons,
            Double supplierFactor) {
        List<TransportMode> all = transportModeRepository.findAll();
        List<TransportMode> validModes = all.stream()
                .filter(m -> m.getEmissionFactorPerKmPerTon() != null && m.getEmissionFactorPerKmPerTon() > 0)
                .collect(java.util.stream.Collectors.toList());

        TransportMode best = recommendBestModeBasedOnContext(validModes, distanceKm, cargoWeightTons, supplierFactor);

        double emission = 0;
        if (distanceKm != null && cargoWeightTons != null) {
            emission = calculateTotalEmission(distanceKm, cargoWeightTons,
                    best.getEmissionFactorPerKmPerTon(), supplierFactor);
        }

        return new RecommendResponse(
                best.getMode().name().toLowerCase(),
                "Based on distance & weight (with production)",
                emission,
                "kg CO2e",
                emission,
                emission,
                1.0,
                1.0);
    }

    private TransportMode.ModeType parseMode(String mode) {
        try {
            return TransportMode.ModeType.valueOf(mode.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "unknown mode: " + mode);
        }
    }
}
