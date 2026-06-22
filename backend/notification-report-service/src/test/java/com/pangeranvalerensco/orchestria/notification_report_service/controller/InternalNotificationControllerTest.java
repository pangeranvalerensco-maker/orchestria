package com.pangeranvalerensco.orchestria.notification_report_service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pangeranvalerensco.orchestria.notification_report_service.dto.NotificationSendRequest;
import com.pangeranvalerensco.orchestria.notification_report_service.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
public class InternalNotificationControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private NotificationService notificationService;

    private InternalNotificationController controller;

    private NotificationSendRequest request;
    
    private final String INTERNAL_KEY = "test-internal-api-key";

    @BeforeEach
    void setUp() {
        controller = new InternalNotificationController(notificationService, INTERNAL_KEY);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
        objectMapper = new ObjectMapper();

        request = new NotificationSendRequest();
        request.setTo(List.of("user@example.com"));
        request.setSubject("Kode verifikasi login Orchestria");
        request.setBody("Kode OTP Anda: 123456");
        request.setHtml(false);
    }

    @Test
    void whenValidInternalKeyAndSent_thenReturns200AndCallsService() throws Exception {
        com.pangeranvalerensco.orchestria.notification_report_service.dto.NotificationLogResponse response = new com.pangeranvalerensco.orchestria.notification_report_service.dto.NotificationLogResponse();
        response.setStatus(com.pangeranvalerensco.orchestria.notification_report_service.entity.NotificationStatus.SENT);
        when(notificationService.sendNotification(any(NotificationSendRequest.class), eq("system"))).thenReturn(response);

        mockMvc.perform(post("/api/internal/notifications/email")
                .header("X-Internal-Api-Key", INTERNAL_KEY)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(notificationService, times(1)).sendNotification(any(NotificationSendRequest.class), eq("system"));
    }

    @Test
    void whenValidInternalKeyAndFailed_thenReturns502() throws Exception {
        com.pangeranvalerensco.orchestria.notification_report_service.dto.NotificationLogResponse response = new com.pangeranvalerensco.orchestria.notification_report_service.dto.NotificationLogResponse();
        response.setStatus(com.pangeranvalerensco.orchestria.notification_report_service.entity.NotificationStatus.FAILED);
        when(notificationService.sendNotification(any(NotificationSendRequest.class), eq("system"))).thenReturn(response);

        mockMvc.perform(post("/api/internal/notifications/email")
                .header("X-Internal-Api-Key", INTERNAL_KEY)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Pengiriman email notifikasi gagal"));

        verify(notificationService, times(1)).sendNotification(any(NotificationSendRequest.class), eq("system"));
    }

    @Test
    void whenEmptyInternalKey_thenReturns401() throws Exception {
        mockMvc.perform(post("/api/internal/notifications/email")
                .header("X-Internal-Api-Key", "")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Akses internal ditolak"));

        verify(notificationService, never()).sendNotification(any(NotificationSendRequest.class), anyString());
    }

    @Test
    void whenMissingInternalKey_thenReturns401() throws Exception {
        mockMvc.perform(post("/api/internal/notifications/email")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Akses internal ditolak"));

        verify(notificationService, never()).sendNotification(any(NotificationSendRequest.class), anyString());
    }

    @Test
    void whenWrongInternalKey_thenReturns401() throws Exception {
        mockMvc.perform(post("/api/internal/notifications/email")
                .header("X-Internal-Api-Key", "wrong-key")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Akses internal ditolak"));

        verify(notificationService, never()).sendNotification(any(NotificationSendRequest.class), anyString());
    }
}
