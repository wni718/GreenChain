package com.greenchain.backend.service;

import com.greenchain.backend.dto.CarbonCalculateResponse;
import com.greenchain.backend.model.TransportMode;
import com.greenchain.backend.repository.TransportModeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Carbon emission logic service testing (calculation and recommendation)")
class CarbonLogicServiceTest {

    @Mock
    private TransportModeRepository transportModeRepository;

    @InjectMocks
    private CarbonLogicService carbonLogicService;

    private TransportMode truckMode;
    private TransportMode railMode;
    private TransportMode invalidFactorMode;

    @BeforeEach
    void setUp() {
        truckMode = new TransportMode();
        truckMode.setMode(TransportMode.ModeType.TRUCK);
        truckMode.setDisplayName("Truck Transport");
        truckMode.setEmissionFactorPerKmPerTon(0.2);

        railMode = new TransportMode();
        railMode.setMode(TransportMode.ModeType.RAIL);
        railMode.setDisplayName("Rail Transport");
        railMode.setEmissionFactorPerKmPerTon(0.1);

        invalidFactorMode = new TransportMode();
        invalidFactorMode.setMode(TransportMode.ModeType.AIR);
        invalidFactorMode.setDisplayName("Air Freight");
        invalidFactorMode.setEmissionFactorPerKmPerTon(null);
    }

    @Test
    @DisplayName("Calculate: Supports lowercase mode, normal calculation")
    void testCalculate_success_lowercaseMode() {
        when(transportModeRepository.findByMode(TransportMode.ModeType.TRUCK)).thenReturn(Optional.of(truckMode));

        CarbonCalculateResponse resp = carbonLogicService.calculate(100.0, 10.0, "truck");

        assertEquals("kg CO2e", resp.unit());
        assertEquals(200.0, resp.carbonKgCo2e(), 0.0001);
    }

    @Test
    @DisplayName("Calculate: distance_km is null and returns 400")
    void testCalculate_nullDistance_returnsBadRequest() {
        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> carbonLogicService.calculate(null, 10.0, "truck")
        );
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
    }

    @Test
    @DisplayName("Calculate: mode Illegal return of 400")
    void testCalculate_invalidMode_returnsBadRequest() {
        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> carbonLogicService.calculate(100.0, 10.0, "invalid")
        );
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
    }

    @Test
    @DisplayName("Calculate: If the emission factor is missing, return 400")
    void testCalculate_missingFactor_returnsBadRequest() {
        when(transportModeRepository.findByMode(TransportMode.ModeType.AIR)).thenReturn(Optional.of(invalidFactorMode));

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> carbonLogicService.calculate(100.0, 10.0, "air")
        );
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
    }
}

