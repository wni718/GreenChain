package com.greenchain.backend.service;

import com.greenchain.backend.model.Shipment;
import com.greenchain.backend.model.Supplier;
import com.greenchain.backend.model.TransportMode;
import com.greenchain.backend.repository.ShipmentRepository;
import com.greenchain.backend.repository.SupplierRepository;
import com.greenchain.backend.repository.TransportModeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("碳排放计算服务测试")
class CarbonCalculationServiceTest {

    @Mock
    private ShipmentRepository shipmentRepository;

    @Mock
    private SupplierRepository supplierRepository;

    @Mock
    private TransportModeRepository transportModeRepository;

    @InjectMocks
    private CarbonCalculationService carbonCalculationService;

    private TransportMode truckMode;
    private Supplier testSupplier;

    @BeforeEach
    void setUp() {
        truckMode = new TransportMode();
        truckMode.setId(1L);
        truckMode.setMode(TransportMode.ModeType.TRUCK);
        truckMode.setDisplayName("卡车运输");
        truckMode.setEmissionFactorPerKmPerTon(0.2);

        testSupplier = new Supplier();
        testSupplier.setId(1L);
        testSupplier.setName("测试供应商");
        testSupplier.setEmissionFactorPerUnit(1.0);
    }

    @Test
    @DisplayName("正常计算碳排放：100km × 0.2系数 × 10吨 = 200 kg CO2e")
    void testCalculateEmission_NormalCase() {
        Double result = carbonCalculationService.calculateEmission(100.0, 0.2, 10.0);
        assertEquals(200.0, result, 0.001);
    }

    @Test
    @DisplayName("空值处理：距离为null应返回0")
    void testCalculateEmission_NullDistance() {
        Double result = carbonCalculationService.calculateEmission(null, 0.2, 10.0);
        assertEquals(0.0, result);
    }

    @Test
    @DisplayName("空值处理：排放系数为null应返回0")
    void testCalculateEmission_NullEmissionFactor() {
        Double result = carbonCalculationService.calculateEmission(100.0, null, 10.0);
        assertEquals(0.0, result);
    }

    @Test
    @DisplayName("空值处理：重量为null应返回0")
    void testCalculateEmission_NullWeight() {
        Double result = carbonCalculationService.calculateEmission(100.0, 0.2, null);
        assertEquals(0.0, result);
    }

    @Test
    @DisplayName("空值处理：所有参数都为null应返回0")
    void testCalculateEmission_AllNull() {
        Double result = carbonCalculationService.calculateEmission(null, null, null);
        assertEquals(0.0, result);
    }

    @Test
    @DisplayName("边界值测试：距离为0应返回0")
    void testCalculateEmission_ZeroDistance() {
        Double result = carbonCalculationService.calculateEmission(0.0, 0.2, 10.0);
        assertEquals(0.0, result);
    }

    @Test
    @DisplayName("边界值测试：重量为0应返回0")
    void testCalculateEmission_ZeroWeight() {
        Double result = carbonCalculationService.calculateEmission(100.0, 0.2, 0.0);
        assertEquals(0.0, result);
    }

    @Test
    @DisplayName("边界值测试：排放系数为0应返回0")
    void testCalculateEmission_ZeroEmissionFactor() {
        Double result = carbonCalculationService.calculateEmission(100.0, 0.0, 10.0);
        assertEquals(0.0, result);
    }

    @Test
    @DisplayName("计算运输碳排放：卡车运输100km，10吨货物")
    void testCalculateShipmentEmission_TruckTransport() {
        when(supplierRepository.findById(1L)).thenReturn(Optional.of(testSupplier));
        when(transportModeRepository.findById(1L)).thenReturn(Optional.of(truckMode));
        when(shipmentRepository.save(any(Shipment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Shipment shipment = new Shipment();
        shipment.setSupplier(testSupplier);
        shipment.setTransportMode(truckMode);
        shipment.setDistanceKm(100.0);
        shipment.setCargoWeightTons(10.0);
        shipment.setShipmentDate(LocalDate.now());

        Shipment result = carbonCalculationService.calculateShipmentEmission(shipment);

        assertNotNull(result.getCalculatedCarbonEmission());
        assertEquals(200.0, result.getCalculatedCarbonEmission(), 0.001);
        assertNotNull(result.getCalculationTimestamp());
    }

    @Test
    @DisplayName("计算运输碳排放：海运长距离")
    void testCalculateShipmentEmission_SeaTransport() {
        TransportMode seaMode = new TransportMode();
        seaMode.setId(2L);
        seaMode.setMode(TransportMode.ModeType.SEA);
        seaMode.setDisplayName("海运");
        seaMode.setEmissionFactorPerKmPerTon(0.05);

        when(supplierRepository.findById(1L)).thenReturn(Optional.of(testSupplier));
        when(transportModeRepository.findById(2L)).thenReturn(Optional.of(seaMode));
        when(shipmentRepository.save(any(Shipment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Shipment shipment = new Shipment();
        shipment.setSupplier(testSupplier);
        shipment.setTransportMode(seaMode);
        shipment.setDistanceKm(5000.0);
        shipment.setCargoWeightTons(100.0);
        shipment.setShipmentDate(LocalDate.now());

        Shipment result = carbonCalculationService.calculateShipmentEmission(shipment);

        assertNotNull(result.getCalculatedCarbonEmission());
        assertEquals(25000.0, result.getCalculatedCarbonEmission(), 0.001);
    }

    @Test
    @DisplayName("批量计算碳排放")
    void testCalculateBatchEmissions() {
        when(supplierRepository.findById(1L)).thenReturn(Optional.of(testSupplier));
        when(transportModeRepository.findById(1L)).thenReturn(Optional.of(truckMode));
        when(shipmentRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));

        Shipment shipment1 = new Shipment();
        shipment1.setSupplier(testSupplier);
        shipment1.setTransportMode(truckMode);
        shipment1.setDistanceKm(100.0);
        shipment1.setCargoWeightTons(10.0);
        shipment1.setShipmentDate(LocalDate.now());

        Shipment shipment2 = new Shipment();
        shipment2.setSupplier(testSupplier);
        shipment2.setTransportMode(truckMode);
        shipment2.setDistanceKm(200.0);
        shipment2.setCargoWeightTons(5.0);
        shipment2.setShipmentDate(LocalDate.now());

        var result = carbonCalculationService.calculateBatchEmissions(
            java.util.List.of(shipment1, shipment2)
        );

        assertEquals(2, result.size());
        assertEquals(200.0, result.get(0).getCalculatedCarbonEmission(), 0.001);
        assertEquals(200.0, result.get(1).getCalculatedCarbonEmission(), 0.001);
    }
}
