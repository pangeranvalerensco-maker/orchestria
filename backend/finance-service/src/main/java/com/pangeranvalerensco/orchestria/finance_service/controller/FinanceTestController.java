package com.pangeranvalerensco.orchestria.finance_service.controller;

import com.pangeranvalerensco.orchestria.finance_service.payload.response.ApiResponse;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class FinanceTestController {

    @GetMapping("/api/finance/test/me")
    public ApiResponse<Map<String, Object>> me(Authentication authentication) {
        return ApiResponse.<Map<String, Object>>builder()
                .success(true)
                .message("Token valid di finance-service")
                .data(Map.of(
                        "email", authentication.getName(),
                        "authorities", authentication.getAuthorities()
                ))
                .build();
    }
}