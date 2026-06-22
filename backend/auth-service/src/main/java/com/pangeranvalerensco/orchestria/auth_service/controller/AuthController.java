package com.pangeranvalerensco.orchestria.auth_service.controller;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pangeranvalerensco.orchestria.auth_service.entity.TrustedDevice;
import com.pangeranvalerensco.orchestria.auth_service.payload.request.*;
import com.pangeranvalerensco.orchestria.auth_service.payload.response.*;
import com.pangeranvalerensco.orchestria.auth_service.repository.TrustedDeviceRepository;
import com.pangeranvalerensco.orchestria.auth_service.service.AuthService;
import com.pangeranvalerensco.orchestria.auth_service.service.impl.AuthServiceImpl; // For hashing helper

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    
    private final AuthService authService;
    private final TrustedDeviceRepository trustedDeviceRepository;
    
    @Value("${app.auth.trusted-device.days:7}")
    private int trustedDeviceDays;

    @PreAuthorize("hasAuthority('auth.user.manage')")
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserResponse>> register(
        @Valid @RequestBody RegisterRequest request
    ) {
        ApiResponse<UserResponse> response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResult>> login(
        @Valid @RequestBody LoginRequest request,
        HttpServletRequest httpRequest,
        @RequestHeader(value = "User-Agent", defaultValue = "") String userAgent,
        @CookieValue(value = "orchestria_device", required = false) String trustedDeviceToken
    ) {
        String ipAddress = getClientIp(httpRequest);
        ApiResponse<LoginResult> response = authService.login(request, userAgent, ipAddress, trustedDeviceToken);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/login/verify")
    public ResponseEntity<ApiResponse<AuthResponse>> verifyLoginOtp(
        @Valid @RequestBody OtpVerifyRequest request,
        HttpServletRequest httpRequest,
        @RequestHeader(value = "User-Agent", defaultValue = "") String userAgent
    ) {
        String ipAddress = getClientIp(httpRequest);
        ApiResponse<AuthResponse> response = authService.verifyLoginOtp(request, userAgent, ipAddress);
        
        if (Boolean.TRUE.equals(request.getRememberDevice())) {
            String rawToken = UUID.randomUUID().toString() + "-" + UUID.randomUUID().toString();
            String tokenHash = ((AuthServiceImpl) authService).sha256Hash(rawToken);
            
            Long userId = response.getData().getUser().getId();
            
            TrustedDevice device = TrustedDevice.builder()
                .id(UUID.randomUUID().toString())
                .userId(userId)
                .tokenHash(tokenHash)
                .deviceName(request.getDeviceName() != null && !request.getDeviceName().isEmpty() ? request.getDeviceName() : "Unknown Device")
                .userAgent(userAgent)
                .lastIpAddress(ipAddress)
                .expiresAt(LocalDateTime.now().plusDays(trustedDeviceDays))
                .build();
                
            trustedDeviceRepository.save(device);
            
            ResponseCookie cookie = ResponseCookie.from("orchestria_device", rawToken)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(trustedDeviceDays * 24 * 60 * 60)
                .sameSite("Strict")
                .build();
                
            return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(response);
        }
        
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/otp/resend")
    public ResponseEntity<ApiResponse<OtpResendResponse>> resendOtp(
        @Valid @RequestBody OtpResendRequest request
    ) {
        return ResponseEntity.ok(authService.resendOtp(request));
    }
    
    @PostMapping("/password/forgot")
    public ResponseEntity<ApiResponse<String>> forgotPassword(
        @Valid @RequestBody ForgotPasswordRequest request
    ) {
        return ResponseEntity.ok(authService.forgotPassword(request));
    }
    
    @PostMapping("/password/forgot/verify")
    public ResponseEntity<ApiResponse<ForgotPasswordVerifyResponse>> verifyForgotPassword(
        @Valid @RequestBody ForgotPasswordVerifyRequest request
    ) {
        return ResponseEntity.ok(authService.verifyForgotPassword(request));
    }
    
    @PostMapping("/password/reset")
    public ResponseEntity<ApiResponse<String>> resetPassword(
        @Valid @RequestBody PasswordResetRequest request
    ) {
        return ResponseEntity.ok(authService.resetPassword(request));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> me(Authentication authentication) {
        String email = authentication.getName();
        ApiResponse<UserResponse> response = authService.getCurrentUser(email);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/security")
    public ResponseEntity<ApiResponse<SecuritySettings>> getSecuritySettings(Authentication authentication) {
        String email = authentication.getName();
        return ResponseEntity.ok(authService.getSecuritySettings(email));
    }
    
    @PostMapping("/security/2fa/enable")
    public ResponseEntity<ApiResponse<String>> requestEnableTwoFactor(Authentication authentication) {
        String email = authentication.getName();
        return ResponseEntity.ok(authService.requestEnableTwoFactor(email));
    }
    
    @PostMapping("/security/2fa/enable/confirm")
    public ResponseEntity<ApiResponse<String>> confirmEnableTwoFactor(
        Authentication authentication,
        @Valid @RequestBody OtpVerifyRequest request
    ) {
        String email = authentication.getName();
        return ResponseEntity.ok(authService.confirmEnableTwoFactor(email, request));
    }
    
    @PostMapping("/security/2fa/disable")
    public ResponseEntity<ApiResponse<String>> requestDisableTwoFactor(Authentication authentication) {
        String email = authentication.getName();
        return ResponseEntity.ok(authService.requestDisableTwoFactor(email));
    }
    
    @PostMapping("/security/2fa/disable/confirm")
    public ResponseEntity<ApiResponse<String>> confirmDisableTwoFactor(
        Authentication authentication,
        @Valid @RequestBody OtpVerifyRequest request
    ) {
        String email = authentication.getName();
        return ResponseEntity.ok(authService.confirmDisableTwoFactor(email, request));
    }
    
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip != null ? ip.split(",")[0].trim() : "Unknown";
    }
}
