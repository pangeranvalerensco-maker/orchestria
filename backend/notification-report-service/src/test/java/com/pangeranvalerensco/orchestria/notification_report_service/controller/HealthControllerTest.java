package com.pangeranvalerensco.orchestria.notification_report_service.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit test untuk HealthController menggunakan standalone MockMvc.
 *
 * Menggunakan MockMvcBuilders.standaloneSetup() agar tidak memerlukan
 * full Spring context maupun spring-boot-test-autoconfigure (kompatibel Spring Boot 4.x).
 *
 * Memverifikasi:
 * 1. Endpoint GET /api/notifications/health mengembalikan HTTP 200
 * 2. Response body mengandung field yang diharapkan
 * 3. Service status "UP"
 */
class HealthControllerTest {

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        HealthController controller = new HealthController();
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void whenCallHealthEndpoint_thenReturn200() throws Exception {
        mockMvc.perform(get("/api/notifications/health")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void whenCallHealthEndpoint_thenReturnSuccessTrue() throws Exception {
        mockMvc.perform(get("/api/notifications/health")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void whenCallHealthEndpoint_thenReturnCorrectServiceInfo() throws Exception {
        mockMvc.perform(get("/api/notifications/health")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("UP"))
                .andExpect(jsonPath("$.data.service").value("notification-report-service"))
                .andExpect(jsonPath("$.data.port").value(8005))
                .andExpect(jsonPath("$.data.timestamp").isNotEmpty());
    }

    @Test
    void whenCallHealthEndpoint_thenReturnJsonContentType() throws Exception {
        mockMvc.perform(get("/api/notifications/health"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
    }
}
