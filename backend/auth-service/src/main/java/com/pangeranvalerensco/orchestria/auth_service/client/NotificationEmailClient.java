package com.pangeranvalerensco.orchestria.auth_service.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class NotificationEmailClient {

    private final RestTemplate restTemplate;
    private final String notificationServiceUrl;
    private final String internalApiKey;

    public NotificationEmailClient(
            RestTemplate restTemplate,
            @Value("${app.services.notification-report.url}") String notificationServiceUrl,
            @Value("${app.internal.api-key}") String internalApiKey) {
        this.restTemplate = restTemplate;
        this.notificationServiceUrl = notificationServiceUrl;
        this.internalApiKey = internalApiKey;
    }

    public void sendEmail(String to, String subject, String body) {
        try {
            String url = notificationServiceUrl + "/api/internal/notifications/email";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("X-Internal-Api-Key", internalApiKey);

            Map<String, Object> request = new HashMap<>();
            request.put("to", List.of(to));
            request.put("subject", subject);
            request.put("body", body);
            request.put("html", false);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);

            restTemplate.postForEntity(url, entity, Void.class);
        } catch (Exception e) {
            log.error("Failed to send internal email to notification service: {}", e.getMessage());
            throw new RuntimeException("Gagal mengirim email notifikasi. Silakan coba lagi.");
        }
    }
}
