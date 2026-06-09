package com.pangeranvalerensco.orchestria.request_service.controller;

import java.util.Map;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RequestTestContoller {
    
    @GetMapping("/api/requests/test/me")
    public Map<String, Object> me(Authentication authentication){
        return Map.of(
                "service", "request-service",
                "email", authentication.getName(),
                "authorities", authentication.getAuthorities()
        );
    }
}
