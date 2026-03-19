package com.greenchain.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.greenchain.backend.model.Shipment;
import com.greenchain.backend.model.Supplier;
import com.greenchain.backend.model.TransportMode;
import com.greenchain.backend.model.User;
import com.greenchain.backend.repository.ShipmentRepository;
import com.greenchain.backend.repository.SupplierRepository;
import com.greenchain.backend.repository.TransportModeRepository;
import com.greenchain.backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("运输控制器测试")
class ShipmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ShipmentRepository shipmentRepository;

    @Autowired
    private SupplierRepository supplierRepository;

    @Autowired
    private TransportModeRepository transportModeRepository;

    @Autowired
    private UserRepository userRepository;

    private Supplier testSupplier;
    private TransportMode truckMode;

    @BeforeEach
    void setUp() {
        shipmentRepository.deleteAll();
        supplierRepository.deleteAll();
        transportModeRepository.deleteAll();

        testSupplier = new Supplier();
        testSupplier.setName("测试供应商");
        testSupplier.setCountry("中国");
        testSupplier.setHasEnvironmentalCertification(true);
        testSupplier.setEmissionFactorPerUnit(1.0);
        testSupplier = supplierRepository.save(testSupplier);

        truckMode = new TransportMode();
        truckMode.setMode(TransportMode.ModeType.TRUCK);
        truckMode.setDisplayName("卡车运输");
        truckMode.setEmissionFactorPerKmPerTon(0.2);
        truckMode = transportModeRepository.save(truckMode);
    }

    @Test
    @DisplayName("获取所有运输记录")
    @WithMockUser(roles = "ADMIN")
    void testGetAllShipments() throws Exception {
        mockMvc.perform(get("/api/shipments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        // 创建运输记录
        Shipment shipment = createTestShipment();
        shipmentRepository.save(shipment);

        mockMvc.perform(get("/api/shipments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].origin").value("上海"))
                .andExpect(jsonPath("$[0].destination").value("北京"));
    }

    @Test
    @DisplayName("创建运输记录：成功并自动计算碳排放")
    @WithMockUser(roles = "SUSTAINABILITY_MANAGER")
    void testCreateShipment_Success() throws Exception {
        Shipment shipment = new Shipment();
        shipment.setSupplier(testSupplier);
        shipment.setTransportMode(truckMode);
        shipment.setOrigin("上海");
        shipment.setDestination("北京");
        shipment.setDistanceKm(100.0);
        shipment.setCargoWeightTons(10.0);
        shipment.setShipmentDate(LocalDate.now());

        mockMvc.perform(post("/api/shipments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(shipment)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.origin").value("上海"))
                .andExpect(jsonPath("$.destination").value("北京"))
                .andExpect(jsonPath("$.distanceKm").value(100.0))
                .andExpect(jsonPath("$.cargoWeightTons").value(10.0))
                .andExpect(jsonPath("$.calculatedCarbonEmission").value(200.0))
                .andExpect(jsonPath("$.calculationTimestamp").isNotEmpty());
    }

    @Test
    @DisplayName("创建运输记录：计算碳排放公式验证")
    @WithMockUser(roles = "SUSTAINABILITY_MANAGER")
    void testCreateShipment_CalculationFormula() throws Exception {
        // 测试：50km × 0.2系数 × 5吨 = 50 kg CO2e
        Shipment shipment = new Shipment();
        shipment.setSupplier(testSupplier);
        shipment.setTransportMode(truckMode);
        shipment.setOrigin("A");
        shipment.setDestination("B");
        shipment.setDistanceKm(50.0);
        shipment.setCargoWeightTons(5.0);
        shipment.setShipmentDate(LocalDate.now());

        mockMvc.perform(post("/api/shipments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(shipment)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.calculatedCarbonEmission").value(50.0));
    }

    @Test
    @DisplayName("按供应商ID获取运输记录")
    @WithMockUser(roles = "ADMIN")
    void testGetShipmentsBySupplier() throws Exception {
        Shipment shipment1 = createTestShipment();
        shipment1.setOrigin("上海");
        shipmentRepository.save(shipment1);

        Shipment shipment2 = new Shipment();
        shipment2.setSupplier(testSupplier);
        shipment2.setTransportMode(truckMode);
        shipment2.setOrigin("广州");
        shipment2.setDestination("深圳");
        shipment2.setDistanceKm(100.0);
        shipment2.setCargoWeightTons(10.0);
        shipment2.setShipmentDate(LocalDate.now());
        shipmentRepository.save(shipment2);

        mockMvc.perform(get("/api/shipments/supplier/" + testSupplier.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    @DisplayName("按供应商ID获取运输记录：无记录时返回空列表")
    @WithMockUser(roles = "ADMIN")
    void testGetShipmentsBySupplier_Empty() throws Exception {
        mockMvc.perform(get("/api/shipments/supplier/" + testSupplier.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    @DisplayName("运输接口：需要认证")
    void testShipmentEndpoint_RequiresAuth() throws Exception {
        mockMvc.perform(get("/api/shipments"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("运输接口：VIEWER角色不能创建")
    @WithMockUser(roles = "VIEWER")
    void testShipmentEndpoint_ViewerCannotCreate() throws Exception {
        Shipment shipment = createTestShipment();

        mockMvc.perform(post("/api/shipments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(shipment)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("运输接口：SUPPLIER角色可以创建")
    @WithMockUser(roles = "SUPPLIER")
    void testShipmentEndpoint_SupplierCanCreate() throws Exception {
        Shipment shipment = createTestShipment();

        mockMvc.perform(post("/api/shipments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(shipment)))
                .andExpect(status().isOk());
    }

    private Shipment createTestShipment() {
        Shipment shipment = new Shipment();
        shipment.setSupplier(testSupplier);
        shipment.setTransportMode(truckMode);
        shipment.setOrigin("上海");
        shipment.setDestination("北京");
        shipment.setDistanceKm(100.0);
        shipment.setCargoWeightTons(10.0);
        shipment.setShipmentDate(LocalDate.now());
        return shipment;
    }
}
