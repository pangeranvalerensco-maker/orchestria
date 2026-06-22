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
    
    ApiResponse<AuthResponse> verifyLoginOtp(OtpVerifyRequest request, String userAgent, String ipAddress);
    
    ApiResponse<OtpResendResponse> resendOtp(OtpResendRequest request);
    
    ApiResponse<String> forgotPassword(ForgotPasswordRequest request);
    
    ApiResponse<ForgotPasswordVerifyResponse> verifyForgotPassword(ForgotPasswordVerifyRequest request);
    
    ApiResponse<String> resetPassword(PasswordResetRequest request);
    
    ApiResponse<SecuritySettings> getSecuritySettings(String email);
    
    ApiResponse<String> requestEnableTwoFactor(String email);
    
    ApiResponse<String> confirmEnableTwoFactor(String email, OtpVerifyRequest request);
    
    ApiResponse<String> requestDisableTwoFactor(String email);
    
    ApiResponse<String> confirmDisableTwoFactor(String email, OtpVerifyRequest request);

    ApiResponse<UserResponse> getCurrentUser(String email);
}
