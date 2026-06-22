package com.pangeranvalerensco.orchestria.auth_service.controller;

import com.pangeranvalerensco.orchestria.auth_service.payload.response.ApiResponse;
import com.pangeranvalerensco.orchestria.auth_service.payload.request.SessionDemoLoginRequest;
import com.pangeranvalerensco.orchestria.auth_service.payload.response.SessionDemoResponse;
import com.pangeranvalerensco.orchestria.auth_service.service.SessionDemoService;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth/session-demo")
public class SessionDemoController {

    private final SessionDemoService sessionDemoService;

    @Value("${server.servlet.session.cookie.name:ORCHESTRIA_SESSION_DEMO}")
    private String cookieName;

    @Value("${server.servlet.session.cookie.path:/api/auth/session-demo}")
    private String cookiePath;

    @Value("${server.servlet.session.cookie.secure:false}")
    private boolean cookieSecure;

    public SessionDemoController(SessionDemoService sessionDemoService) {
        this.sessionDemoService = sessionDemoService;
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<SessionDemoResponse>> login(
            @Valid @RequestBody SessionDemoLoginRequest request,
            HttpServletRequest httpRequest) {
        
        SessionDemoResponse response = sessionDemoService.login(request, httpRequest);
        
        return ResponseEntity.ok()
                .header(HttpHeaders.CACHE_CONTROL, "no-store")
                .body(ApiResponse.<SessionDemoResponse>builder()
                        .success(true)
                        .message(response.getMessage())
                        .data(response)
                        .build());
    }

    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<SessionDemoResponse>> getProfile(HttpServletRequest httpRequest) {
        SessionDemoResponse response = sessionDemoService.getProfile(httpRequest);
        
        return ResponseEntity.ok()
                .header(HttpHeaders.CACHE_CONTROL, "no-store")
                .body(ApiResponse.<SessionDemoResponse>builder()
                        .success(true)
                        .message(response.getMessage())
                        .data(response)
                        .build());
    }

    @GetMapping("/status")
    public ResponseEntity<ApiResponse<SessionDemoResponse>> getStatus(HttpServletRequest httpRequest) {
        SessionDemoResponse response = sessionDemoService.getStatus(httpRequest);
        
        return ResponseEntity.ok()
                .header(HttpHeaders.CACHE_CONTROL, "no-store")
                .body(ApiResponse.<SessionDemoResponse>builder()
                        .success(response.isAuthenticated())
                        .message(response.getMessage())
                        .data(response)
                        .build());
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<SessionDemoResponse>> logout(
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse) {
        
        SessionDemoResponse response = sessionDemoService.logout(httpRequest);
        
        Cookie clearCookie = new Cookie(cookieName, null);
        clearCookie.setPath(cookiePath);
        clearCookie.setHttpOnly(true);
        clearCookie.setSecure(cookieSecure);
        clearCookie.setMaxAge(0);
        httpResponse.addCookie(clearCookie);

        return ResponseEntity.ok()
                .header(HttpHeaders.CACHE_CONTROL, "no-store")
                .body(ApiResponse.<SessionDemoResponse>builder()
                        .success(true)
                        .message(response.getMessage())
                        .data(response)
                        .build());
    }
}
