package com.pangeranvalerensco.orchestria.auth_service.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pangeranvalerensco.orchestria.auth_service.payload.request.*;
import com.pangeranvalerensco.orchestria.auth_service.payload.response.*;
import com.pangeranvalerensco.orchestria.auth_service.service.AuthService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    
    private static final String TRUSTED_DEVICE_COOKIE = "ORCHESTRIA_TRUSTED_DEVICE";

    private final AuthService authService;
    
    @Value("${app.auth.trusted-device.days:7}")
    private int trustedDeviceDays;
    
    @Value("${app.auth.trusted-device.cookie-secure:false}")
    private boolean trustedDeviceCookieSecure;

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
        @CookieValue(value = TRUSTED_DEVICE_COOKIE, required = false) String trustedDeviceToken
    ) {
        String ipAddress = getClientIp(httpRequest);
        ApiResponse<LoginResult> response = authService.login(request, userAgent, ipAddress, trustedDeviceToken);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/otp/verify")
    public ResponseEntity<ApiResponse<AuthResponse>> verifyOtp(
        @Valid @RequestBody OtpVerifyRequest request,
        HttpServletRequest httpRequest,
        @RequestHeader(value = "User-Agent", defaultValue = "") String userAgent
    ) {
        String ipAddress = getClientIp(httpRequest);
        
        ApiResponse<AuthResponse> response = authService.verifyOtp(request, userAgent, ipAddress);
        
        if (Boolean.TRUE.equals(request.getRememberDevice())) {
            String rawToken = authService.createTrustedDevice(response.getData().getUser().getId(), request.getDeviceName(), userAgent, ipAddress);
            if (rawToken != null) {
                ResponseCookie cookie = ResponseCookie.from(TRUSTED_DEVICE_COOKIE, rawToken)
                    .httpOnly(true)
                    .secure(trustedDeviceCookieSecure)
                    .path("/api/auth")
                    .maxAge(trustedDeviceDays * 24 * 60 * 60)
                    .sameSite("Lax")
                    .build();
                    
                return ResponseEntity.ok()
                    .header(HttpHeaders.SET_COOKIE, cookie.toString())
                    .body(response);
            }
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
    public ResponseEntity<ApiResponse<ForgotPasswordStartResponse>> forgotPassword(
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
    
    @PostMapping("/2fa/enable/request")
    public ResponseEntity<ApiResponse<String>> requestEnableTwoFactor(Authentication authentication) {
        String email = authentication.getName();
        return ResponseEntity.ok(authService.requestEnableTwoFactor(email));
    }
    
    @PostMapping("/2fa/enable/confirm")
    public ResponseEntity<ApiResponse<String>> confirmEnableTwoFactor(
        Authentication authentication,
        @Valid @RequestBody OtpVerifyRequest request
    ) {
        String email = authentication.getName();
        return ResponseEntity.ok(authService.confirmEnableTwoFactor(email, request));
    }
    
    @PostMapping("/2fa/disable/request")
    public ResponseEntity<ApiResponse<String>> requestDisableTwoFactor(Authentication authentication) {
        String email = authentication.getName();
        return ResponseEntity.ok(authService.requestDisableTwoFactor(email));
    }
    
    @PostMapping("/2fa/disable/confirm")
    public ResponseEntity<ApiResponse<String>> confirmDisableTwoFactor(
        Authentication authentication,
        @Valid @RequestBody OtpVerifyRequest request
    ) {
        String email = authentication.getName();
        return ResponseEntity.ok(authService.confirmDisableTwoFactor(email, request));
    }
    
    @GetMapping("/trusted-devices")
    public ResponseEntity<ApiResponse<java.util.List<TrustedDeviceResponse>>> getTrustedDevices(Authentication authentication) {
        String email = authentication.getName();
        return ResponseEntity.ok(authService.getTrustedDevices(email));
    }
    
    @DeleteMapping("/trusted-devices/{id}")
    public ResponseEntity<ApiResponse<String>> revokeTrustedDevice(Authentication authentication, @org.springframework.web.bind.annotation.PathVariable String id) {
        String email = authentication.getName();
        return ResponseEntity.ok(authService.revokeTrustedDevice(email, id));
    }
    
    @DeleteMapping("/trusted-devices")
    public ResponseEntity<ApiResponse<String>> revokeAllTrustedDevices(Authentication authentication) {
        String email = authentication.getName();
        return ResponseEntity.ok(authService.revokeAllTrustedDevices(email));
    }
    
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<String>> logout(
        Authentication authentication,
        @CookieValue(value = TRUSTED_DEVICE_COOKIE, required = false) String trustedDeviceToken
    ) {
        String email = authentication.getName();
        ApiResponse<String> response = authService.logout(email, trustedDeviceToken);
        
        ResponseCookie cookie = ResponseCookie.from(TRUSTED_DEVICE_COOKIE, "")
            .httpOnly(true)
            .secure(trustedDeviceCookieSecure)
            .path("/api/auth")
            .maxAge(0)
            .sameSite("Lax")
            .build();
            
        return ResponseEntity.ok()
            .header(HttpHeaders.SET_COOKIE, cookie.toString())
            .body(response);
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
