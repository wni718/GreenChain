package com.greenchain.backend.service;

import com.greenchain.backend.model.Shipment;
import com.greenchain.backend.model.TransportMode;
import com.greenchain.backend.repository.ShipmentRepository;
import com.greenchain.backend.repository.SupplierRepository;
import com.greenchain.backend.repository.TransportModeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

@Service
public class DashboardService {

    @Autowired
    private ShipmentRepository shipmentRepository;

    @Autowired
    private SupplierRepository supplierRepository;

    @Autowired
    private TransportModeRepository transportModeRepository;

    /**
     * Aggregated KPIs and series for dashboard charts (work package: overview + time trends + mode split).
     * Date series uses shipmentDate when set; otherwise falls back to calculationTimestamp so charts show data
     * even when shipmentDate was omitted.
     */
    public Map<String, Object> buildSummary() {
        long shipmentCount = shipmentRepository.count();
        long supplierCount = supplierRepository.count();
        long certifiedCount = supplierRepository.countByHasEnvironmentalCertificationTrue();
        Long inChain = shipmentRepository.countDistinctSuppliersInShipments();
        if (inChain == null) {
            inChain = 0L;
        }

        double maxFactor = transportModeRepository.findAll().stream()
                .map(TransportMode::getEmissionFactorPerKmPerTon)
                .filter(Objects::nonNull)
                .mapToDouble(Double::doubleValue)
                .max()
                .orElse(0.0);

        List<Shipment> all = shipmentRepository.findAll();

        double totalEmissions = 0.0;
        TreeMap<LocalDate, Double> emissionsByDate = new TreeMap<>();
        Map<String, Double> emissionsByModeLabel = new HashMap<>();

        double baselineWorstCaseKg = 0.0;
        double estimatedAvoidedKg = 0.0;

        for (Shipment s : all) {
            Double actual = s.getCalculatedCarbonEmission();
            if (actual != null) {
                totalEmissions += actual;
            }

            LocalDate day = s.getShipmentDate();
            if (day == null && s.getCalculationTimestamp() != null) {
                day = s.getCalculationTimestamp().toLocalDate();
            }
            if (day != null && actual != null) {
                emissionsByDate.merge(day, actual, Double::sum);
            }

            String modeLabel = modeLabel(s);
            if (actual != null) {
                emissionsByModeLabel.merge(modeLabel, actual, Double::sum);
            }

            Double d = s.getDistanceKm();
            Double w = s.getCargoWeightTons();
            if (d != null && w != null && maxFactor > 0) {
                double worst = d * w * maxFactor;
                baselineWorstCaseKg += worst;
                if (actual != null) {
                    estimatedAvoidedKg += Math.max(0.0, worst - actual);
                }
            }
        }

        double reductionPercentVsWorstMode = baselineWorstCaseKg > 0
                ? (estimatedAvoidedKg / baselineWorstCaseKg) * 100.0
                : 0.0;

        List<Map<String, Object>> byMode = new ArrayList<>();
        emissionsByModeLabel.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(e -> {
                    Map<String, Object> item = new HashMap<>();
                    item.put("transportMode", e.getKey());
                    item.put("emissionsKg", round2(e.getValue()));
                    byMode.add(item);
                });

        List<Map<String, Object>> byDate = new ArrayList<>();
        for (Map.Entry<LocalDate, Double> e : emissionsByDate.entrySet()) {
            Map<String, Object> item = new HashMap<>();
            item.put("date", e.getKey().toString());
            item.put("emissionsKg", round2(e.getValue()));
            byDate.add(item);
        }

        Map<String, Object> out = new HashMap<>();
        out.put("totalEmissionsKg", round2(totalEmissions));
        out.put("emissionsUnit", "kg CO2e");
        out.put("shipmentCount", shipmentCount);
        out.put("registeredSupplierCount", supplierCount);
        out.put("certifiedSupplierCount", certifiedCount);
        out.put("enterprisesInSupplyChain", inChain);
        out.put("estimatedAvoidedEmissionsKg", round2(estimatedAvoidedKg));
        out.put("reductionPercentVsHighestFactorMode", round2(reductionPercentVsWorstMode));
        out.put("emissionsByTransportMode", byMode);
        out.put("emissionsByShipmentDate", byDate);
        return out;
    }

    private static String modeLabel(Shipment s) {
        TransportMode tm = s.getTransportMode();
        if (tm == null) {
            return "Unknown";
        }
        if (tm.getDisplayName() != null && !tm.getDisplayName().isBlank()) {
            return tm.getDisplayName();
        }
        if (tm.getMode() != null) {
            return tm.getMode().name();
        }
        return "Unknown";
    }

    private static double round2(double v) {
        return Math.round(v * 100.0) / 100.0;
    }
}
