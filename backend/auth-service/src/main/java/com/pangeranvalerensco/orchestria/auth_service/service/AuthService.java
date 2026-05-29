package com.pangeranvalerensco.orchestria.auth_service.service;

import com.pangeranvalerensco.orchestria.auth_service.payload.request.RegisterRequest;
import com.pangeranvalerensco.orchestria.auth_service.payload.response.ApiResponse;
import com.pangeranvalerensco.orchestria.auth_service.payload.response.UserResponse;

public interface AuthService {
    
    ApiResponse<UserResponse> register(RegisterRequest request);
}
