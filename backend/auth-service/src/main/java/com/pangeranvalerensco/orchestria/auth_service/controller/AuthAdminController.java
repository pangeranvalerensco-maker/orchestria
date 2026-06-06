package com.pangeranvalerensco.orchestria.auth_service.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pangeranvalerensco.orchestria.auth_service.payload.response.ApiResponse;
import com.pangeranvalerensco.orchestria.auth_service.payload.response.PermissionsResponse;
import com.pangeranvalerensco.orchestria.auth_service.payload.response.RoleResponse;
import com.pangeranvalerensco.orchestria.auth_service.payload.response.UserResponse;
import com.pangeranvalerensco.orchestria.auth_service.service.AuthAdminService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('SUPER_ADMIN')")
public class AuthAdminController {
    
    private final AuthAdminService authAdminService;

    @GetMapping("/users")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getAllUsers(){
        return ResponseEntity.ok(authAdminService.getAllUsers());
    }

    @GetMapping("/roles")
    public ResponseEntity<ApiResponse<List<RoleResponse>>> getAllRoles(){
        return ResponseEntity.ok(authAdminService.getAllRoles());
    }

    @GetMapping("/permissions")
    public ResponseEntity<ApiResponse<List<PermissionsResponse>>> getAllPermissions(){
        return ResponseEntity.ok(authAdminService.getAllPermissions());
    }

    @PostMapping("/users/{userId}/roles/{roleName}")
    public ResponseEntity<ApiResponse<UserResponse>> assignRoleToUser(
            @PathVariable Long userId,
            @PathVariable String roleName
    ){
        return ResponseEntity.ok(authAdminService.assignRoleToUser(userId, roleName));
    }

    @DeleteMapping("/users/{userId}/roles/{roleName}")
    public ResponseEntity<ApiResponse<UserResponse>> removeRoleFromUser(
            @PathVariable Long userId,
            @PathVariable String roleName
    ){
        return ResponseEntity.ok(authAdminService.removeRoleFromUser(userId, roleName));
    }
}
