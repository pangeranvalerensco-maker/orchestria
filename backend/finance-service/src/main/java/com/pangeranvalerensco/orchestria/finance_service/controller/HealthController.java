package com.pangeranvalerensco.orchestria.finance_service.controller;

import com.pangeranvalerensco.orchestria.finance_service.payload.response.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class HealthController {

    @GetMapping("/health")
    public ApiResponse<Map<String, Object>> health() {
        return ApiResponse.<Map<String, Object>>builder()
                .success(true)
                .message("Finance service is running")
                .data(Map.of(
                        "service", "finance-service",
                        "status", "UP"
                ))
                .build();
    }
}