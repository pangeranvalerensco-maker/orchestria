package com.pangeranvalerensco.orchestria.organization_service.security;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import com.pangeranvalerensco.orchestria.organization_service.payload.response.ErrorResponse;

import java.io.IOException;
import java.time.LocalDateTime;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import tools.jackson.databind.ObjectMapper;

@Component
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void commence(
            HttpServletRequest request, 
            HttpServletResponse response, 
            AuthenticationException authException
    ) throws IOException {
        
        ErrorResponse<String> errorResponse = ErrorResponse.<String>builder()
                .success(false)
                .status(HttpStatus.UNAUTHORIZED.value())
                .error(HttpStatus.UNAUTHORIZED.getReasonPhrase())
                .message("Anda belum login atau Token tidak valid")
                .path(request.getRequestURI())
                .timestamp(LocalDateTime.now())
                .details(null)
                .build();

        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        objectMapper.writeValue(response.getWriter(), errorResponse);
    }
    
}
