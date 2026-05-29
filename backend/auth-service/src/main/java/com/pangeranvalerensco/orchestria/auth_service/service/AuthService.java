package com.pangeranvalerensco.orchestria.auth_service.service;

import com.pangeranvalerensco.orchestria.auth_service.payload.request.LoginRequest;
import com.pangeranvalerensco.orchestria.auth_service.payload.request.RegisterRequest;
import com.pangeranvalerensco.orchestria.auth_service.payload.response.ApiResponse;
import com.pangeranvalerensco.orchestria.auth_service.payload.response.AuthResponse;
import com.pangeranvalerensco.orchestria.auth_service.payload.response.UserResponse;

public interface AuthService {
    
    ApiResponse<UserResponse> register(RegisterRequest request);

    ApiResponse<AuthResponse> login(LoginRequest request);

    ApiResponse<UserResponse> getCurrentUser(String email);
}
