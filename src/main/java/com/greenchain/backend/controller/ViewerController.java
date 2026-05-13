package com.greenchain.backend.controller;

import com.greenchain.backend.model.Shipment;
import com.greenchain.backend.model.Supplier;
import com.greenchain.backend.repository.ShipmentRepository;
import com.greenchain.backend.repository.SupplierRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/viewer")
@PreAuthorize("hasRole('VIEWER')")
public class ViewerController {

    @Autowired
    private ShipmentRepository shipmentRepository;

    @Autowired
    private SupplierRepository supplierRepository;

    /**
     * 获取仪表板概览数据 - VIEWER专属
     */
    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> getDashboardOverview() {
        Map<String, Object> overview = new HashMap<>();

        // 总发货数
        long totalShipments = shipmentRepository.count();
        overview.put("totalShipments", totalShipments);

        // 总供应商数
        long totalSuppliers = supplierRepository.count();
        overview.put("totalSuppliers", totalSuppliers);

        // 认证供应商数
        long certifiedSuppliers = supplierRepository.countByHasEnvironmentalCertificationTrue();
        overview.put("certifiedSuppliers", certifiedSuppliers);

        // 供应链中的企业数
        Long enterprisesInChain = shipmentRepository.countDistinctSuppliersInShipments();
        overview.put("enterprisesInChain", enterprisesInChain != null ? enterprisesInChain : 0);

        // 总碳排放量
        Double totalEmissions = shipmentRepository.sumAllCarbonEmissions();
        overview.put("totalEmissionsKg", totalEmissions != null ? totalEmissions : 0);
        overview.put("emissionsUnit", "kg CO2e");

        // 按运输方式统计排放量
        List<Shipment> allShipments = shipmentRepository.findAll();
        Map<String, Double> emissionsByMode = new HashMap<>();

        for (Shipment shipment : allShipments) {
            String mode = shipment.getTransportMode() != null && shipment.getTransportMode().getMode() != null
                    ? shipment.getTransportMode().getMode().name()
                    : "Unknown";
            double emissions = shipment.getCalculatedCarbonEmission() != null
                    ? shipment.getCalculatedCarbonEmission()
                    : 0.0;
            emissionsByMode.merge(mode, emissions, Double::sum);
        }
        overview.put("emissionsByTransportMode", emissionsByMode);

        // 最近发货记录
        List<Shipment> recentShipments = shipmentRepository.findTop10ByOrderByShipmentDateDesc();
        List<Map<String, Object>> recentShipmentList = recentShipments.stream()
                .map(this::convertShipmentToSummary)
                .collect(Collectors.toList());
        overview.put("recentShipments", recentShipmentList);

        return ResponseEntity.ok(overview);
    }

    /**
     * 获取碳排放量统计详情
     */
    @GetMapping("/carbon-stats")
    public ResponseEntity<Map<String, Object>> getCarbonStats(
            @RequestParam(required = false) String period) {

        Map<String, Object> stats = new HashMap<>();
        List<Shipment> allShipments = shipmentRepository.findAll();

        // 总排放量
        double totalEmissions = allShipments.stream()
                .mapToDouble(s -> s.getCalculatedCarbonEmission() != null ? s.getCalculatedCarbonEmission() : 0)
                .sum();
        stats.put("totalEmissionsKg", totalEmissions);

        // 平均每次发货排放量
        double avgEmissions = allShipments.isEmpty() ? 0 : totalEmissions / allShipments.size();
        stats.put("averageEmissionsPerShipmentKg", avgEmissions);

        // 按运输方式统计
        Map<String, Double> byMode = new HashMap<>();
        Map<String, Long> countByMode = new HashMap<>();

        for (Shipment s : allShipments) {
            String mode = s.getTransportMode() != null && s.getTransportMode().getMode() != null
                    ? s.getTransportMode().getMode().name()
                    : "Unknown";
            double emissions = s.getCalculatedCarbonEmission() != null ? s.getCalculatedCarbonEmission() : 0;
            byMode.merge(mode, emissions, Double::sum);
            countByMode.merge(mode, 1L, Long::sum);
        }
        stats.put("emissionsByMode", byMode);
        stats.put("shipmentCountByMode", countByMode);

        // 按日期统计（最近30天）
        Map<LocalDate, Double> byDate = new TreeMap<>();
        LocalDate today = LocalDate.now();

        for (int i = 29; i >= 0; i--) {
            byDate.put(today.minusDays(i), 0.0);
        }

        for (Shipment s : allShipments) {
            if (s.getShipmentDate() != null) {
                LocalDate date = s.getShipmentDate();
                if (byDate.containsKey(date)) {
                    double emissions = s.getCalculatedCarbonEmission() != null ? s.getCalculatedCarbonEmission() : 0;
                    byDate.merge(date, emissions, Double::sum);
                }
            }
        }

        List<Map<String, Object>> dateEmissions = new ArrayList<>();
        byDate.forEach((date, emissions) -> {
            Map<String, Object> item = new HashMap<>();
            item.put("date", date.toString());
            item.put("emissionsKg", emissions);
            dateEmissions.add(item);
        });
        stats.put("emissionsByDate", dateEmissions);

        return ResponseEntity.ok(stats);
    }

    /**
     * 获取供应商概览
     */
    @GetMapping("/suppliers-overview")
    public ResponseEntity<Map<String, Object>> getSuppliersOverview() {
        Map<String, Object> overview = new HashMap<>();

        List<Supplier> allSuppliers = supplierRepository.findAll();

        // 供应商总数
        overview.put("totalSuppliers", allSuppliers.size());

        // 认证供应商数和比例
        long certifiedCount = supplierRepository.countByHasEnvironmentalCertificationTrue();
        overview.put("certifiedSuppliers", certifiedCount);
        overview.put("certificationRate",
                allSuppliers.isEmpty() ? 0 : Math.round((certifiedCount * 100.0 / allSuppliers.size()) * 100) / 100.0);

        // 按国家统计
        Map<String, Long> byCountry = allSuppliers.stream()
                .filter(s -> s.getCountry() != null && !s.getCountry().isEmpty())
                .collect(Collectors.groupingBy(Supplier::getCountry, Collectors.counting()));
        overview.put("suppliersByCountry", byCountry);

        // 供应商列表（基础信息）
        List<Map<String, Object>> supplierList = allSuppliers.stream()
                .map(this::convertSupplierToSummary)
                .collect(Collectors.toList());
        overview.put("suppliers", supplierList);

        return ResponseEntity.ok(overview);
    }

    /**
     * 获取发货记录概览
     */
    @GetMapping("/shipments-overview")
    public ResponseEntity<Map<String, Object>> getShipmentsOverview(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {

        int pageNum = page != null ? Math.max(0, page) : 0;
        int pageSize = size != null ? Math.min(Math.max(1, size), 50) : 10;

        Map<String, Object> overview = new HashMap<>();

        List<Shipment> allShipments = shipmentRepository.findAll();
        overview.put("totalCount", allShipments.size());

        // 分页发货记录
        int fromIndex = pageNum * pageSize;
        int toIndex = Math.min(fromIndex + pageSize, allShipments.size());
        List<Shipment> paginatedShipments = fromIndex < allShipments.size()
                ? allShipments.subList(fromIndex, toIndex)
                : Collections.emptyList();

        List<Map<String, Object>> shipmentList = paginatedShipments.stream()
                .map(this::convertShipmentToDetail)
                .collect(Collectors.toList());
        overview.put("shipments", shipmentList);

        // 分页信息
        overview.put("currentPage", pageNum);
        overview.put("pageSize", pageSize);
        overview.put("totalPages", (int) Math.ceil((double) allShipments.size() / pageSize));

        return ResponseEntity.ok(overview);
    }

    /**
     * 获取环保建议摘要
     */
    @GetMapping("/eco-tips")
    public ResponseEntity<Map<String, Object>> getEcoTips() {
        List<Map<String, Object>> tips = new ArrayList<>();

        // 获取数据用于生成建议
        List<Shipment> shipments = shipmentRepository.findAll();
        long certifiedSuppliers = supplierRepository.countByHasEnvironmentalCertificationTrue();
        long totalSuppliers = supplierRepository.count();

        // 建议1：使用认证供应商
        if (totalSuppliers > 0) {
            double certifiedRate = (certifiedSuppliers * 100.0) / totalSuppliers;
            Map<String, Object> tip1 = new HashMap<>();
            tip1.put("title", "优先选择认证供应商");
            tip1.put("description", String.format("目前 %.1f%% 的供应商已获得环保认证，建议优先与认证供应商合作以减少碳排放。", certifiedRate));
            tip1.put("importance", certifiedRate < 50 ? "high" : "medium");
            tips.add(tip1);
        }

        // 建议2：优化运输方式
        Map<String, Long> modeCount = shipments.stream()
                .filter(s -> s.getTransportMode() != null && s.getTransportMode().getMode() != null)
                .collect(Collectors.groupingBy(
                        s -> s.getTransportMode().getMode().name(),
                        Collectors.counting()));

        if (!modeCount.isEmpty()) {
            Map<String, Object> tip2 = new HashMap<>();
            tip2.put("title", "优化运输方式组合");
            tip2.put("description", "当前发货运输方式分布：" + modeCount.toString() + "。建议根据距离和货物类型选择更环保的运输方式。");
            tip2.put("importance", "medium");
            tips.add(tip2);
        }

        // 建议3：减少单次运输重量
        double avgWeight = shipments.stream()
                .filter(s -> s.getCargoWeightTons() != null)
                .mapToDouble(Shipment::getCargoWeightTons)
                .average()
                .orElse(0);

        Map<String, Object> tip3 = new HashMap<>();
        tip3.put("title", "优化货物装载率");
        tip3.put("description", String.format("当前平均每单货物重量为 %.2f 吨。合理规划装载量可以提高运输效率，减少碳排放。", avgWeight));
        tip3.put("importance", "low");
        tips.add(tip3);

        Map<String, Object> result = new HashMap<>();
        result.put("tips", tips);
        result.put("generatedAt", LocalDate.now().toString());

        return ResponseEntity.ok(result);
    }

    /**
     * 搜索发货记录
     */
    @GetMapping("/search/shipments")
    public ResponseEntity<List<Map<String, Object>>> searchShipments(
            @RequestParam(required = false) String keyword) {

        List<Shipment> shipments = shipmentRepository.findAll();

        if (keyword != null && !keyword.isEmpty()) {
            String lowerKeyword = keyword.toLowerCase();
            shipments = shipments.stream()
                    .filter(s -> (s.getOrigin() != null && s.getOrigin().toLowerCase().contains(lowerKeyword)) ||
                            (s.getDestination() != null && s.getDestination().toLowerCase().contains(lowerKeyword)) ||
                            (s.getSupplier() != null && s.getSupplier().getName() != null &&
                                    s.getSupplier().getName().toLowerCase().contains(lowerKeyword)))
                    .collect(Collectors.toList());
        }

        List<Map<String, Object>> result = shipments.stream()
                .map(this::convertShipmentToDetail)
                .collect(Collectors.toList());

        return ResponseEntity.ok(result);
    }

    /**
     * 搜索供应商
     */
    @GetMapping("/search/suppliers")
    public ResponseEntity<List<Map<String, Object>>> searchSuppliers(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Boolean certified) {

        List<Supplier> suppliers = supplierRepository.findAll();

        if (keyword != null && !keyword.isEmpty()) {
            String lowerKeyword = keyword.toLowerCase();
            suppliers = suppliers.stream()
                    .filter(s -> (s.getName() != null && s.getName().toLowerCase().contains(lowerKeyword)) ||
                            (s.getCountry() != null && s.getCountry().toLowerCase().contains(lowerKeyword)))
                    .collect(Collectors.toList());
        }

        if (certified != null) {
            suppliers = suppliers.stream()
                    .filter(s -> certified.equals(s.getHasEnvironmentalCertification()))
                    .collect(Collectors.toList());
        }

        List<Map<String, Object>> result = suppliers.stream()
                .map(this::convertSupplierToSummary)
                .collect(Collectors.toList());

        return ResponseEntity.ok(result);
    }

    private Map<String, Object> convertShipmentToSummary(Shipment shipment) {
        Map<String, Object> summary = new HashMap<>();
        summary.put("id", shipment.getId());
        summary.put("origin", shipment.getOrigin());
        summary.put("destination", shipment.getDestination());
        summary.put("shipmentDate", shipment.getShipmentDate());
        summary.put("calculatedCarbonEmission", shipment.getCalculatedCarbonEmission());
        if (shipment.getTransportMode() != null) {
            summary.put("transportMode", shipment.getTransportMode().getMode() != null
                    ? shipment.getTransportMode().getMode().name()
                    : null);
        }
        if (shipment.getSupplier() != null) {
            summary.put("supplierName", shipment.getSupplier().getName());
        }
        return summary;
    }

    private Map<String, Object> convertShipmentToDetail(Shipment shipment) {
        Map<String, Object> detail = new HashMap<>();
        detail.put("id", shipment.getId());
        detail.put("origin", shipment.getOrigin());
        detail.put("destination", shipment.getDestination());
        detail.put("distanceKm", shipment.getDistanceKm());
        detail.put("cargoWeightTons", shipment.getCargoWeightTons());
        detail.put("shipmentDate", shipment.getShipmentDate());
        detail.put("calculatedCarbonEmission", shipment.getCalculatedCarbonEmission());
        detail.put("calculationTimestamp", shipment.getCalculationTimestamp());

        if (shipment.getTransportMode() != null) {
            detail.put("transportMode", shipment.getTransportMode().getMode() != null
                    ? shipment.getTransportMode().getMode().name()
                    : null);
            detail.put("transportModeDisplayName", shipment.getTransportMode().getDisplayName());
        }

        if (shipment.getSupplier() != null) {
            Map<String, Object> supplierInfo = new HashMap<>();
            supplierInfo.put("id", shipment.getSupplier().getId());
            supplierInfo.put("name", shipment.getSupplier().getName());
            supplierInfo.put("country", shipment.getSupplier().getCountry());
            supplierInfo.put("hasEnvironmentalCertification",
                    shipment.getSupplier().getHasEnvironmentalCertification());
            detail.put("supplier", supplierInfo);
        }

        return detail;
    }

    private Map<String, Object> convertSupplierToSummary(Supplier supplier) {
        Map<String, Object> summary = new HashMap<>();
        summary.put("id", supplier.getId());
        summary.put("name", supplier.getName());
        summary.put("country", supplier.getCountry());
        summary.put("hasEnvironmentalCertification", supplier.getHasEnvironmentalCertification());
        summary.put("emissionFactorPerUnit", supplier.getEmissionFactorPerUnit());
        summary.put("contactEmail", supplier.getContactEmail());
        return summary;
    }
}