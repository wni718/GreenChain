package com.greenchain.backend.service;

import com.greenchain.backend.model.Supplier;
import com.greenchain.backend.model.TransportMode;
import com.greenchain.backend.repository.SupplierRepository;
import com.greenchain.backend.repository.TransportModeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("优化建议服务测试")
class OptimizationServiceTest {

    @Mock
    private SupplierRepository supplierRepository;

    @Mock
    private TransportModeRepository transportModeRepository;

    @InjectMocks
    private OptimizationService optimizationService;

    private Supplier highEmissionSupplier;
    private Supplier lowEmissionSupplier;
    private TransportMode truckMode;
    private TransportMode railMode;
    private TransportMode seaMode;

    @BeforeEach
    void setUp() {
        highEmissionSupplier = new Supplier();
        highEmissionSupplier.setId(1L);
        highEmissionSupplier.setName("高排放供应商");
        highEmissionSupplier.setEmissionFactorPerUnit(1.0);

        lowEmissionSupplier = new Supplier();
        lowEmissionSupplier.setId(2L);
        lowEmissionSupplier.setName("绿色供应商");
        lowEmissionSupplier.setEmissionFactorPerUnit(0.5);

        truckMode = new TransportMode();
        truckMode.setId(1L);
        truckMode.setMode(TransportMode.ModeType.TRUCK);
        truckMode.setDisplayName("卡车运输");
        truckMode.setEmissionFactorPerKmPerTon(0.2);

        railMode = new TransportMode();
        railMode.setId(2L);
        railMode.setMode(TransportMode.ModeType.RAIL);
        railMode.setDisplayName("铁路运输");
        railMode.setEmissionFactorPerKmPerTon(0.1);

        seaMode = new TransportMode();
        seaMode.setId(3L);
        seaMode.setMode(TransportMode.ModeType.SEA);
        seaMode.setDisplayName("海运");
        seaMode.setEmissionFactorPerKmPerTon(0.05);
    }

    // ==================== 供应商优化测试 ====================

    @Test
    @DisplayName("推荐更环保的供应商：有更优选择时返回推荐")
    void testRecommendGreenerSupplier_WithBetterOption() {
        when(supplierRepository.findById(1L)).thenReturn(Optional.of(highEmissionSupplier));
        when(supplierRepository.findAll()).thenReturn(List.of(highEmissionSupplier, lowEmissionSupplier));

        Map<String, Object> result = optimizationService.recommendGreenerSupplier(1L);

        assertTrue((Boolean) result.get("hasRecommendation"));
        assertEquals("绿色供应商", result.get("recommendedSupplier"));
        assertEquals(1.0, result.get("currentEmissionFactor"));
        assertEquals(0.5, result.get("recommendedEmissionFactor"));
        assertTrue(result.get("message").toString().contains("减少"));
    }

    @Test
    @DisplayName("推荐更环保的供应商：当前已是最优时无推荐")
    void testRecommendGreenerSupplier_AlreadyBest() {
        when(supplierRepository.findById(2L)).thenReturn(Optional.of(lowEmissionSupplier));
        when(supplierRepository.findAll()).thenReturn(List.of(highEmissionSupplier, lowEmissionSupplier));

        Map<String, Object> result = optimizationService.recommendGreenerSupplier(2L);

        assertFalse((Boolean) result.get("hasRecommendation"));
        assertEquals("当前供应商已是最环保的选择", result.get("message"));
    }

    @Test
    @DisplayName("推荐更环保的供应商：供应商不存在时返回错误")
    void testRecommendGreenerSupplier_NotFound() {
        when(supplierRepository.findById(999L)).thenReturn(Optional.empty());

        Map<String, Object> result = optimizationService.recommendGreenerSupplier(999L);

        assertTrue(result.containsKey("error"));
        assertEquals("Supplier not found", result.get("error"));
    }

    @Test
    @DisplayName("推荐更环保的供应商：计算减排百分比")
    void testRecommendGreenerSupplier_CalculateReductionPercentage() {
        when(supplierRepository.findById(1L)).thenReturn(Optional.of(highEmissionSupplier));
        when(supplierRepository.findAll()).thenReturn(List.of(highEmissionSupplier, lowEmissionSupplier));

        Map<String, Object> result = optimizationService.recommendGreenerSupplier(1L);

        // 高排放1.0 -> 绿色0.5，减少50%
        assertEquals("50.0", result.get("reductionPercentage"));
    }

    // ==================== 运输方式优化测试 ====================

    @Test
    @DisplayName("推荐更环保的运输方式：有更优选择时返回推荐")
    void testRecommendGreenerTransport_WithBetterOption() {
        when(transportModeRepository.findAll()).thenReturn(List.of(truckMode, railMode, seaMode));

        // TRUCK(0.2) -> 推荐 SEA(0.05)，因为海运最环保
        Map<String, Object> result = optimizationService.recommendGreenerTransport("TRUCK");

        assertTrue((Boolean) result.get("hasRecommendation"));
        assertEquals("海运", result.get("recommendedMode"));
        assertEquals(0.2, result.get("currentEmissionFactor"));
        assertEquals(0.05, result.get("recommendedEmissionFactor"));
        assertTrue(result.get("message").toString().contains("减少"));
    }

    @Test
    @DisplayName("推荐更环保的运输方式：当前已是最优时无推荐")
    void testRecommendGreenerTransport_AlreadyBest() {
        when(transportModeRepository.findAll()).thenReturn(List.of(truckMode, railMode, seaMode));

        Map<String, Object> result = optimizationService.recommendGreenerTransport("SEA");

        assertFalse((Boolean) result.get("hasRecommendation"));
        assertEquals("当前运输方式已是最环保的选择", result.get("message"));
    }

    @Test
    @DisplayName("推荐更环保的运输方式：运输方式不存在时返回错误")
    void testRecommendGreenerTransport_NotFound() {
        when(transportModeRepository.findAll()).thenReturn(List.of(truckMode, railMode, seaMode));

        Map<String, Object> result = optimizationService.recommendGreenerTransport("AIRPLANE");

        assertTrue(result.containsKey("error"));
        assertEquals("Transport mode not found", result.get("error"));
    }

    @Test
    @DisplayName("推荐更环保的运输方式：大小写不敏感")
    void testRecommendGreenerTransport_CaseInsensitive() {
        when(transportModeRepository.findAll()).thenReturn(List.of(truckMode, railMode, seaMode));

        Map<String, Object> resultLower = optimizationService.recommendGreenerTransport("truck");
        Map<String, Object> resultUpper = optimizationService.recommendGreenerTransport("TRUCK");
        Map<String, Object> resultMixed = optimizationService.recommendGreenerTransport("Truck");

        assertEquals(resultUpper.get("hasRecommendation"), resultLower.get("hasRecommendation"));
        assertEquals(resultUpper.get("hasRecommendation"), resultMixed.get("hasRecommendation"));
    }

    @Test
    @DisplayName("推荐更环保的运输方式：卡车改海运减排最多")
    void testRecommendGreenerTransport_TruckToSeaMaximumReduction() {
        when(transportModeRepository.findAll()).thenReturn(List.of(truckMode, railMode, seaMode));

        Map<String, Object> result = optimizationService.recommendGreenerTransport("TRUCK");

        assertEquals("海运", result.get("recommendedMode"));
        // 0.2 -> 0.05，减少 75%
        assertEquals("75.0", result.get("reductionPercentage"));
    }

    @Test
    @DisplayName("推荐更环保的运输方式：推荐最环保的选项")
    void testRecommendGreenerTransport_AlwaysRecommendGreenest() {
        when(transportModeRepository.findAll()).thenReturn(List.of(truckMode, railMode, seaMode));

        Map<String, Object> resultTruck = optimizationService.recommendGreenerTransport("TRUCK");
        Map<String, Object> resultRail = optimizationService.recommendGreenerTransport("RAIL");

        // 两者都应该推荐海运（最环保）
        assertEquals("海运", resultTruck.get("recommendedMode"));
        assertEquals("海运", resultRail.get("recommendedMode"));
    }
}
