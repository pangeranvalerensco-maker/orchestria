package com.pangeranvalerensco.orchestria.auth_service.security;

import java.io.IOException;
import java.time.LocalDateTime;

import tools.jackson.databind.ObjectMapper;
import com.pangeranvalerensco.orchestria.auth_service.payload.response.ErrorResponse;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;


@Component
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException
    ) throws IOException, ServletException {
        ErrorResponse<Object> errorResponse = ErrorResponse.builder()
                    .success(false)
                    .status(HttpStatus.UNAUTHORIZED.value())
                    .error(HttpStatus.UNAUTHORIZED.getReasonPhrase())
                    .message("Anda belum login atau token tidak valid")
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
