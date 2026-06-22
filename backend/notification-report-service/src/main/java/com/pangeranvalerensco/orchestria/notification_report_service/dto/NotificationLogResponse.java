package com.pangeranvalerensco.orchestria.notification_report_service.dto;

import com.pangeranvalerensco.orchestria.notification_report_service.entity.NotificationLog;
import com.pangeranvalerensco.orchestria.notification_report_service.entity.NotificationStatus;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class NotificationLogResponse {
    private String id;
    private List<String> toRecipients;
    private List<String> ccRecipients;
    private List<String> bccRecipients;
    private String subject;
    private String body;
    private boolean html;
    private NotificationStatus status;
    private int attemptCount;
    private String lastError;
    private LocalDateTime nextRetryAt;
    private String createdByEmail;
    private LocalDateTime createdAt;
    private LocalDateTime lastAttemptAt;
    private LocalDateTime sentAt;

    public static NotificationLogResponse fromEntity(NotificationLog entity) {
        NotificationLogResponse response = new NotificationLogResponse();
        response.setId(entity.getId());
        response.setToRecipients(entity.getToRecipients());
        response.setCcRecipients(entity.getCcRecipients());
        response.setBccRecipients(entity.getBccRecipients());
        response.setSubject(entity.getSubject());
        response.setBody(entity.getBody());
        response.setHtml(entity.isHtml());
        response.setStatus(entity.getStatus());
        response.setAttemptCount(entity.getAttemptCount());
        response.setLastError(entity.getLastError());
        response.setNextRetryAt(entity.getNextRetryAt());
        response.setCreatedByEmail(entity.getCreatedByEmail());
        response.setCreatedAt(entity.getCreatedAt());
        response.setLastAttemptAt(entity.getLastAttemptAt());
        response.setSentAt(entity.getSentAt());
        return response;
    }
}
