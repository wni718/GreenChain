package com.greenchain.backend.service;

import com.greenchain.backend.model.Supplier;
import com.greenchain.backend.model.TransportMode;
import com.greenchain.backend.repository.SupplierRepository;
import com.greenchain.backend.repository.TransportModeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class OptimizationService {

    @Autowired
    private SupplierRepository supplierRepository;

    @Autowired
    private TransportModeRepository transportModeRepository;

    // Recommend more environmentally friendly suppliers
    public Map<String, Object> recommendGreenerSupplier(Long currentSupplierId) {
        Optional<Supplier> currentOpt = supplierRepository.findById(currentSupplierId);
        if (currentOpt.isEmpty()) {
            return Map.of("error", "Supplier not found");
        }

        Supplier current = currentOpt.get();
        List<Supplier> allSuppliers = supplierRepository.findAll();

        Optional<Supplier> greener = allSuppliers.stream()
                .filter(s -> !s.getId().equals(currentSupplierId))
                .filter(s -> s.getEmissionFactorPerUnit() != null)
                .filter(s -> s.getEmissionFactorPerUnit() < current.getEmissionFactorPerUnit())
                .min((s1, s2) -> s1.getEmissionFactorPerUnit().compareTo(s2.getEmissionFactorPerUnit()));

        Map<String, Object> result = new HashMap<>();
        if (greener.isPresent()) {
            Supplier better = greener.get();
            Double reduction = ((current.getEmissionFactorPerUnit() - better.getEmissionFactorPerUnit())
                    / current.getEmissionFactorPerUnit()) * 100;

            result.put("hasRecommendation", true);
            result.put("recommendedSupplier", better.getName());
            result.put("currentEmissionFactor", current.getEmissionFactorPerUnit());
            result.put("recommendedEmissionFactor", better.getEmissionFactorPerUnit());
            result.put("reductionPercentage", String.format("%.1f", reduction));
            result.put("message", String.format("If you choose %s, it can reduce carbon emissions by %.1f%%",
                    better.getName(), reduction));
        } else {
            result.put("hasRecommendation", false);
            result.put("message", "The current supplier is already the most environmentally friendly choice");
        }
        return result;
    }

    // Recommend more environmentally friendly transportation methods
    public Map<String, Object> recommendGreenerTransport(String currentMode) {
        List<TransportMode> modes = transportModeRepository.findAll();

        Optional<TransportMode> currentOpt = modes.stream()
                .filter(m -> m.getMode().name().equalsIgnoreCase(currentMode))
                .findFirst();

        if (currentOpt.isEmpty()) {
            return Map.of("error", "Transport mode not found");
        }

        TransportMode current = currentOpt.get();
        Optional<TransportMode> greener = modes.stream()
                .filter(m -> !m.getMode().name().equalsIgnoreCase(currentMode))
                .filter(m -> m.getEmissionFactorPerKmPerTon() < current.getEmissionFactorPerKmPerTon())
                .min((m1, m2) -> m1.getEmissionFactorPerKmPerTon().compareTo(m2.getEmissionFactorPerKmPerTon()));

        Map<String, Object> result = new HashMap<>();
        if (greener.isPresent()) {
            TransportMode better = greener.get();
            Double reduction = ((current.getEmissionFactorPerKmPerTon() - better.getEmissionFactorPerKmPerTon())
                    / current.getEmissionFactorPerKmPerTon()) * 100;

            result.put("hasRecommendation", true);
            result.put("recommendedMode", better.getDisplayName());
            result.put("currentEmissionFactor", current.getEmissionFactorPerKmPerTon());
            result.put("recommendedEmissionFactor", better.getEmissionFactorPerKmPerTon());
            result.put("reductionPercentage", String.format("%.1f", reduction));
            result.put("message", String.format("If changed to %s, it can reduce carbon emissions by %.1f%%",
                    better.getDisplayName(), reduction));
        } else {
            result.put("hasRecommendation", false);
            result.put("message", "The current mode of transportation is already the most environmentally friendly choice");
        }
        return result;
    }
}