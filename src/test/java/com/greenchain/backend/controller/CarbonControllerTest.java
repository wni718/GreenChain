package com.greenchain.backend.controller;

import com.greenchain.backend.model.Shipment;
import com.greenchain.backend.model.Supplier;
import com.greenchain.backend.model.TransportMode;
import com.greenchain.backend.repository.ShipmentRepository;
import com.greenchain.backend.repository.SupplierRepository;
import com.greenchain.backend.repository.TransportModeRepository;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("碳排放控制器测试")
class CarbonControllerTest {

    @Autowired
    private MockMvc mockMvc;

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

        seaMode = new TransportMode();
        seaMode.setMode(TransportMode.ModeType.SEA);
        seaMode.setDisplayName("海运");
        seaMode.setEmissionFactorPerKmPerTon(0.05);
        seaMode = transportModeRepository.save(seaMode);
    }

    @Test
    @DisplayName("获取碳排放总量：无数据时返回0")
    @WithMockUser(roles = "ADMIN")
    void testGetTotalEmissions_Empty() throws Exception {
        mockMvc.perform(get("/api/carbon/total"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalEmissions").value(0))
                .andExpect(jsonPath("$.unit").value("kg CO2e"));
    }

    @Test
    @DisplayName("获取碳排放总量：有数据时返回总和")
    @WithMockUser(roles = "ADMIN")
    void testGetTotalEmissions_WithData() throws Exception {
        // 创建运输记录
        Shipment shipment = new Shipment();
        shipment.setSupplier(testSupplier);
        shipment.setTransportMode(truckMode);
        shipment.setOrigin("上海");
        shipment.setDestination("北京");
        shipment.setDistanceKm(100.0);
        shipment.setCargoWeightTons(10.0);
        shipment.setShipmentDate(LocalDate.now());
        shipment.setCalculatedCarbonEmission(200.0);
        shipmentRepository.save(shipment);

        mockMvc.perform(get("/api/carbon/total"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalEmissions").value(200.0))
                .andExpect(jsonPath("$.unit").value("kg CO2e"));
    }

    @Test
    @DisplayName("获取碳排放总量：多条记录求和")
    @WithMockUser(roles = "ADMIN")
    void testGetTotalEmissions_MultipleShipments() throws Exception {
        Shipment shipment1 = new Shipment();
        shipment1.setSupplier(testSupplier);
        shipment1.setTransportMode(truckMode);
        shipment1.setDistanceKm(100.0);
        shipment1.setCargoWeightTons(10.0);
        shipment1.setShipmentDate(LocalDate.now());
        shipment1.setCalculatedCarbonEmission(200.0);
        shipmentRepository.save(shipment1);

        Shipment shipment2 = new Shipment();
        shipment2.setSupplier(testSupplier);
        shipment2.setTransportMode(seaMode);
        shipment2.setDistanceKm(500.0);
        shipment2.setCargoWeightTons(50.0);
        shipment2.setShipmentDate(LocalDate.now());
        shipment2.setCalculatedCarbonEmission(1250.0);
        shipmentRepository.save(shipment2);

        mockMvc.perform(get("/api/carbon/total"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalEmissions").value(1450.0));
    }

    @Test
    @DisplayName("优化供应商：推荐更环保的供应商")
    @WithMockUser(roles = "SUSTAINABILITY_MANAGER")
    void testOptimizeSupplier_WithRecommendation() throws Exception {
        // 添加一个更环保的供应商
        Supplier greenSupplier = new Supplier();
        greenSupplier.setName("绿色供应商");
        greenSupplier.setCountry("中国");
        greenSupplier.setHasEnvironmentalCertification(true);
        greenSupplier.setEmissionFactorPerUnit(0.5);
        supplierRepository.save(greenSupplier);

        mockMvc.perform(get("/api/carbon/optimize/supplier/" + testSupplier.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.hasRecommendation").value(true))
                .andExpect(jsonPath("$.recommendedSupplier").value("绿色供应商"));
    }

    @Test
    @DisplayName("优化供应商：供应商不存在")
    @WithMockUser(roles = "SUSTAINABILITY_MANAGER")
    void testOptimizeSupplier_NotFound() throws Exception {
        mockMvc.perform(get("/api/carbon/optimize/supplier/9999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.error").value("Supplier not found"));
    }

    @Test
    @DisplayName("优化运输方式：推荐更环保的方式")
    @WithMockUser(roles = "SUSTAINABILITY_MANAGER")
    void testOptimizeTransport_WithRecommendation() throws Exception {
        mockMvc.perform(get("/api/carbon/optimize/transport/TRUCK"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.hasRecommendation").value(true));
    }

    @Test
    @DisplayName("优化运输方式：当前已是最环保的")
    @WithMockUser(roles = "SUSTAINABILITY_MANAGER")
    void testOptimizeTransport_AlreadyBest() throws Exception {
        mockMvc.perform(get("/api/carbon/optimize/transport/SEA"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.hasRecommendation").value(false))
                .andExpect(jsonPath("$.message").value("当前运输方式已是最环保的选择"));
    }

    @Test
    @DisplayName("优化运输方式：运输方式不存在")
    @WithMockUser(roles = "SUSTAINABILITY_MANAGER")
    void testOptimizeTransport_NotFound() throws Exception {
        mockMvc.perform(get("/api/carbon/optimize/transport/INVALID"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.error").value("Transport mode not found"));
    }

    @Test
    @DisplayName("Carbon emissions calculation: POST /api/carbon/calculate normal calculation")
    void testCarbonCalculate_Success() throws Exception {
        mockMvc.perform(post("/api/carbon/calculate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"distance_km\":100.0,\"cargo_weight_tons\":10.0,\"mode\":\"truck\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.carbon_kg_co2e").value(200.0))
                .andExpect(jsonPath("$.unit").value("kg CO2e"));
    }

    @Test
    @DisplayName("Carbon emissions calculation: Unknown transportation method returns 400")
    void testCarbonCalculate_UnknownMode() throws Exception {
        mockMvc.perform(post("/api/carbon/calculate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"distance_km\":100.0,\"cargo_weight_tons\":10.0,\"mode\":\"invalid\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Carbon emissions calculation: Missing distance_km, return 400")
    void testCarbonCalculate_MissingDistance() throws Exception {
        mockMvc.perform(post("/api/carbon/calculate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"distance_km\":null,\"cargo_weight_tons\":10.0,\"mode\":\"truck\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("碳排放接口：需要认证")
    void testCarbonEndpoint_RequiresAuth() throws Exception {
        mockMvc.perform(get("/api/carbon/total"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("碳排放接口：VIEWER角色可以访问")
    @WithMockUser(roles = "VIEWER")
    void testCarbonEndpoint_ViewerCanAccess() throws Exception {
        mockMvc.perform(get("/api/carbon/total"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("碳排放接口：SUSTAINABILITY_MANAGER角色可以访问")
    @WithMockUser(roles = "SUSTAINABILITY_MANAGER")
    void testCarbonEndpoint_SupplierCanAccess() throws Exception {
        mockMvc.perform(get("/api/carbon/optimize/transport/TRUCK"))
                .andExpect(status().isOk());
    }
}
