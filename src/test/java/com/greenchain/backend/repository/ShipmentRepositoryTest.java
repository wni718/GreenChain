package com.greenchain.backend.repository;

import com.greenchain.backend.model.Shipment;
import com.greenchain.backend.model.Supplier;
import com.greenchain.backend.model.TransportMode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@DisplayName("运输仓库测试")
class ShipmentRepositoryTest {

    @Autowired
    private ShipmentRepository shipmentRepository;

    @Autowired
    private SupplierRepository supplierRepository;

    @Autowired
    private TransportModeRepository transportModeRepository;

    private Supplier testSupplier;
    private TransportMode truckMode;
    private TransportMode seaMode;

    @BeforeEach
    void setUp() {
        shipmentRepository.deleteAll();
        supplierRepository.deleteAll();
        transportModeRepository.deleteAll();

        testSupplier = supplierRepository.save(createSupplier("供应商A", 1.0));
        Supplier supplierB = supplierRepository.save(createSupplier("供应商B", 0.8));

        truckMode = transportModeRepository.save(createTransportMode(TransportMode.ModeType.TRUCK, 0.2));
        seaMode = transportModeRepository.save(createTransportMode(TransportMode.ModeType.SEA, 0.05));
    }

    @Test
    @DisplayName("按供应商ID查询运输记录")
    void testFindBySupplierId() {
        shipmentRepository.save(createShipment(testSupplier, truckMode, 100.0, 10.0, 200.0));
        shipmentRepository.save(createShipment(testSupplier, seaMode, 500.0, 50.0, 1250.0));
        Supplier otherSupplier = supplierRepository.save(createSupplier("其他供应商", 1.5));
        shipmentRepository.save(createShipment(otherSupplier, truckMode, 200.0, 20.0, 800.0));

        List<Shipment> result = shipmentRepository.findBySupplierId(testSupplier.getId());

        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(s -> s.getSupplier().getId().equals(testSupplier.getId())));
    }

    @Test
    @DisplayName("按供应商ID查询：无记录时返回空列表")
    void testFindBySupplierId_Empty() {
        List<Shipment> result = shipmentRepository.findBySupplierId(testSupplier.getId());
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("按供应商ID查询：不存在供应商返回空列表")
    void testFindBySupplierId_NotFound() {
        List<Shipment> result = shipmentRepository.findBySupplierId(9999L);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("计算所有碳排放总量：有数据")
    void testSumAllCarbonEmissions() {
        shipmentRepository.save(createShipment(testSupplier, truckMode, 100.0, 10.0, 200.0));
        shipmentRepository.save(createShipment(testSupplier, seaMode, 500.0, 50.0, 1250.0));

        Double total = shipmentRepository.sumAllCarbonEmissions();

        assertEquals(1450.0, total, 0.001);
    }

    @Test
    @DisplayName("计算所有碳排放总量：无数据返回null")
    void testSumAllCarbonEmissions_Empty() {
        Double total = shipmentRepository.sumAllCarbonEmissions();
        assertNull(total);
    }

    @Test
    @DisplayName("按运输方式查询运输记录")
    void testFindByTransportMode() {
        shipmentRepository.save(createShipment(testSupplier, truckMode, 100.0, 10.0, 200.0));
        shipmentRepository.save(createShipment(testSupplier, seaMode, 500.0, 50.0, 1250.0));

        List<Shipment> truckShipments = shipmentRepository.findByTransportModeId(truckMode.getId());
        List<Shipment> seaShipments = shipmentRepository.findByTransportModeId(seaMode.getId());

        assertEquals(1, truckShipments.size());
        assertEquals(1, seaShipments.size());
    }

    @Test
    @DisplayName("保存运输记录：自动生成ID")
    void testSave_SetsId() {
        Shipment shipment = createShipment(testSupplier, truckMode, 100.0, 10.0, 200.0);
        assertNull(shipment.getId());

        Shipment saved = shipmentRepository.save(shipment);

        assertNotNull(saved.getId());
    }

    @Test
    @DisplayName("批量保存运输记录")
    void testSaveAll() {
        List<Shipment> shipments = List.of(
            createShipment(testSupplier, truckMode, 100.0, 10.0, 200.0),
            createShipment(testSupplier, seaMode, 500.0, 50.0, 1250.0)
        );

        List<Shipment> saved = shipmentRepository.saveAll(shipments);

        assertEquals(2, saved.size());
        assertTrue(saved.stream().noneMatch(s -> s.getId() == null));
    }

    private Supplier createSupplier(String name, Double emissionFactor) {
        Supplier supplier = new Supplier();
        supplier.setName(name);
        supplier.setCountry("中国");
        supplier.setHasEnvironmentalCertification(true);
        supplier.setEmissionFactorPerUnit(emissionFactor);
        return supplier;
    }

    private TransportMode createTransportMode(TransportMode.ModeType modeType, Double factor) {
        TransportMode transportMode = new TransportMode();
        transportMode.setMode(modeType);
        transportMode.setDisplayName(modeType.name());
        transportMode.setEmissionFactorPerKmPerTon(factor);
        return transportMode;
    }

    private Shipment createShipment(Supplier supplier, TransportMode mode, Double distance, Double weight, Double emission) {
        Shipment shipment = new Shipment();
        shipment.setSupplier(supplier);
        shipment.setTransportMode(mode);
        shipment.setOrigin("A");
        shipment.setDestination("B");
        shipment.setDistanceKm(distance);
        shipment.setCargoWeightTons(weight);
        shipment.setShipmentDate(LocalDate.now());
        shipment.setCalculatedCarbonEmission(emission);
        return shipment;
    }
}
