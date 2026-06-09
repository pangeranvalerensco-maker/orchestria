package com.pangeranvalerensco.orchestria.request_service.controller;

import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {
    
    @GetMapping("/")
    public Map<String, Object> home(){
        return Map.of(
                "service", "request-service",
                "status", "running"
        );
    }

    @GetMapping("/health")
    public Map<String, Object> health(){
        return Map.of(
                "service", "request-service",
                "status", "UP"
        );
    }
}
