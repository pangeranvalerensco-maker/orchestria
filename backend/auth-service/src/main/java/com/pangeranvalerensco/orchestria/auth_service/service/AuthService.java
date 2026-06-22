package com.pangeranvalerensco.orchestria.auth_service.service;

import com.pangeranvalerensco.orchestria.auth_service.payload.request.LoginRequest;
import com.pangeranvalerensco.orchestria.auth_service.payload.request.RegisterRequest;
import com.pangeranvalerensco.orchestria.auth_service.payload.response.ApiResponse;
import com.pangeranvalerensco.orchestria.auth_service.payload.response.AuthResponse;
import com.pangeranvalerensco.orchestria.auth_service.payload.response.UserResponse;
import com.pangeranvalerensco.orchestria.auth_service.payload.request.*;
import com.pangeranvalerensco.orchestria.auth_service.payload.response.*;
public interface AuthService {
    
    ApiResponse<UserResponse> register(RegisterRequest request);

    ApiResponse<LoginResult> login(LoginRequest request, String userAgent, String ipAddress, String trustedDeviceToken);
    
    ApiResponse<AuthResponse> verifyOtp(OtpVerifyRequest request, String userAgent, String ipAddress);
    
    ApiResponse<OtpResendResponse> resendOtp(OtpResendRequest request);
    
    ApiResponse<ForgotPasswordStartResponse> forgotPassword(ForgotPasswordRequest request);
    
    ApiResponse<ForgotPasswordVerifyResponse> verifyForgotPassword(ForgotPasswordVerifyRequest request);
    
    ApiResponse<String> resetPassword(PasswordResetRequest request);
    
    ApiResponse<SecuritySettings> getSecuritySettings(String email);
    
    ApiResponse<String> requestEnableTwoFactor(String email);
    
    ApiResponse<String> confirmEnableTwoFactor(String email, OtpVerifyRequest request);
    
    ApiResponse<String> requestDisableTwoFactor(String email);
    
    ApiResponse<String> confirmDisableTwoFactor(String email, OtpVerifyRequest request);

    ApiResponse<UserResponse> getCurrentUser(String email);
    
    String createTrustedDevice(Long userId, String deviceName, String userAgent, String ipAddress);
    
    ApiResponse<java.util.List<TrustedDeviceResponse>> getTrustedDevices(String email);
    
    ApiResponse<String> revokeTrustedDevice(String email, String id);
    
    ApiResponse<String> revokeAllTrustedDevices(String email);
    
    ApiResponse<String> logout(String email, String trustedDeviceToken);
}
