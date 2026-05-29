package com.pangeranvalerensco.orchestria.auth_service.controller;

import java.time.LocalDateTime;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {
    
    @GetMapping("/health")
    public Map<String, Object> health(){
        return Map.of(
            "service", "auth-service",
            "status", "UP",
            "timestamp", LocalDateTime.now().toString()
        );
    }
}
