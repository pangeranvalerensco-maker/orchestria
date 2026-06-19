package com.pangeranvalerensco.orchestria.notification_report_service.controller;

import com.pangeranvalerensco.orchestria.notification_report_service.payload.response.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Controller untuk endpoint health check.
 *
 * Endpoint:
 *   GET /api/notifications/health
 */
@Slf4j
@RestController
@RequestMapping("/api/notifications")
public class HealthController {

    /**
     * Health check endpoint.
     *
     * @return HTTP 200 dengan status dan timestamp
     */
    @GetMapping("/health")
    public ResponseEntity<ApiResponse<Map<String, Object>>> health() {
        log.debug("[HEALTH] Health check endpoint dipanggil.");

        Map<String, Object> info = Map.of(
                "service", "notification-report-service",
                "status", "UP",
                "timestamp", LocalDateTime.now().toString(),
                "port", 8005
        );

        return ResponseEntity.ok(ApiResponse.<Map<String, Object>>builder()
                .success(true)
                .message("Service berjalan normal")
                .data(info)
                .build());
    }
}
