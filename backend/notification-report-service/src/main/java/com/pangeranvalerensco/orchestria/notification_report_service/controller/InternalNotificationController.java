package com.pangeranvalerensco.orchestria.notification_report_service.controller;

import com.pangeranvalerensco.orchestria.notification_report_service.dto.NotificationSendRequest;
import com.pangeranvalerensco.orchestria.notification_report_service.service.NotificationService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/internal/notifications")
public class InternalNotificationController {

    private final NotificationService notificationService;
    private final String internalApiKey;

    public InternalNotificationController(
            NotificationService notificationService,
            @Value("${app.internal.api-key}") String internalApiKey) {
        this.notificationService = notificationService;
        this.internalApiKey = internalApiKey;
    }

    @PostMapping("/email")
    public ResponseEntity<?> sendEmailInternal(
            @RequestHeader(value = "X-Internal-Api-Key", required = false) String apiKey,
            @RequestBody NotificationSendRequest request) {
        
        if (apiKey == null || apiKey.isEmpty() || !java.security.MessageDigest.isEqual(apiKey.getBytes(java.nio.charset.StandardCharsets.UTF_8), internalApiKey.getBytes(java.nio.charset.StandardCharsets.UTF_8))) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(java.util.Map.of(
                "success", false,
                "message", "Akses internal ditolak"
            ));
        }

        notificationService.sendNotification(request, "system");
        return ResponseEntity.ok().build();
    }
}
