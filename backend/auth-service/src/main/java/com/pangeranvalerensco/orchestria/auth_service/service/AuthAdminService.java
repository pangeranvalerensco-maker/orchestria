package com.pangeranvalerensco.orchestria.auth_service.service;

import com.pangeranvalerensco.orchestria.auth_service.payload.response.ApiResponse;
import com.pangeranvalerensco.orchestria.auth_service.payload.response.PermissionsResponse;
import com.pangeranvalerensco.orchestria.auth_service.payload.response.RoleResponse;
import com.pangeranvalerensco.orchestria.auth_service.payload.response.UserResponse;

import java.util.List;

public interface AuthAdminService {
    
    ApiResponse<List<UserResponse>> getAllUsers();
    ApiResponse<List<RoleResponse>> getAllRoles();
    ApiResponse<List<PermissionsResponse>> getAllPermissions();
    ApiResponse<UserResponse> assignRoleToUser(Long userId, String roleName);
    ApiResponse<UserResponse> removeRoleFromUser(Long userId, String roleName);
}
