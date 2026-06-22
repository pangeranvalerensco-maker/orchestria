package com.pangeranvalerensco.orchestria.notification_report_service.controller;

import com.pangeranvalerensco.orchestria.notification_report_service.dto.NotificationLogResponse;
import com.pangeranvalerensco.orchestria.notification_report_service.dto.NotificationSendRequest;
import com.pangeranvalerensco.orchestria.notification_report_service.service.NotificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import com.pangeranvalerensco.orchestria.notification_report_service.entity.NotificationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @PostMapping("/email")
    @PreAuthorize("hasAuthority('notification.send')")
    public ResponseEntity<Void> sendNotification(@Valid @RequestBody NotificationSendRequest request, Authentication authentication) {
        String email = authentication.getName();
        notificationService.sendNotification(request, email);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/logs/{id}/retry")
    @PreAuthorize("hasAuthority('notification.retry')")
    public ResponseEntity<Void> retryNotification(@PathVariable String id, Authentication authentication) {
        String email = authentication.getName();
        notificationService.retryNotification(id, email);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/logs")
    @PreAuthorize("hasAuthority('notification.read')")
    public ResponseEntity<Page<NotificationLogResponse>> getNotificationLogs(
            @RequestParam(required = false) NotificationStatus status,
            Pageable pageable, 
            Authentication authentication) {
        String email = authentication.getName();
        return ResponseEntity.ok(notificationService.getNotificationLogs(email, status, pageable));
    }

    @GetMapping("/logs/{id}")
    @PreAuthorize("hasAuthority('notification.read')")
    public ResponseEntity<NotificationLogResponse> getNotificationLogDetail(@PathVariable String id, Authentication authentication) {
        String email = authentication.getName();
        return ResponseEntity.ok(notificationService.getNotificationLogDetail(id, email));
    }
}
