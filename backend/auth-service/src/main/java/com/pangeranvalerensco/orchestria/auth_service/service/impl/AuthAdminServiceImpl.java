package com.pangeranvalerensco.orchestria.auth_service.service.impl;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.pangeranvalerensco.orchestria.auth_service.entity.Permission;
import com.pangeranvalerensco.orchestria.auth_service.entity.Role;
import com.pangeranvalerensco.orchestria.auth_service.entity.User;
import com.pangeranvalerensco.orchestria.auth_service.exception.BadRequestException;
import com.pangeranvalerensco.orchestria.auth_service.exception.ResourceNotFoundException;
import com.pangeranvalerensco.orchestria.auth_service.payload.response.ApiResponse;
import com.pangeranvalerensco.orchestria.auth_service.payload.response.PermissionsResponse;
import com.pangeranvalerensco.orchestria.auth_service.payload.response.RoleResponse;
import com.pangeranvalerensco.orchestria.auth_service.payload.response.UserResponse;
import com.pangeranvalerensco.orchestria.auth_service.repository.RoleRepository;
import com.pangeranvalerensco.orchestria.auth_service.repository.UserRepository;
import com.pangeranvalerensco.orchestria.auth_service.repository.PermissionRepository;
import com.pangeranvalerensco.orchestria.auth_service.service.AuthAdminService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthAdminServiceImpl implements AuthAdminService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;

    @Override
    public ApiResponse<List<UserResponse>> getAllUsers() {
        List<UserResponse> users = userRepository.findAll()
                        .stream()
                        .map(this::mapToUserResponse)
                        .toList();
        return ApiResponse.<List<UserResponse>>builder()
                .success(true)
                .message("Daftar User berhasil diambil")
                .data(users)
                .build();
    }

    @Override
    public ApiResponse<List<RoleResponse>> getAllRoles() {
        List<RoleResponse> roles = roleRepository.findAll()
                        .stream()
                        .map(this::mapToRoleResponse)
                        .toList();
        return ApiResponse.<List<RoleResponse>>builder()
                .success(true)
                .message("Daftar Role berhasil diambil")
                .data(roles)
                .build();
    }

    @Override
    public ApiResponse<List<PermissionsResponse>> getAllPermissions() {
        List<PermissionsResponse> permissions = permissionRepository.findAll()
                        .stream()
                        .map(this::mapToPermissionsResponse)
                        .toList();
        return ApiResponse.<List<PermissionsResponse>>builder()
                .success(true)
                .message("Daftar Permission berhasil diambil")
                .data(permissions)
                .build();
    }

    @Override
    public ApiResponse<UserResponse> assignRoleToUser(Long userId, String roleName) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User tidak ditemukan"));

        Role role = roleRepository.findByName(roleName.toUpperCase())
                .orElseThrow(() -> new ResourceNotFoundException("Role tidak ditemukan"));

        if(user.getRoles().contains(role)){
            throw new BadRequestException("User sudah memiliki Role ini " + role.getName());
        }

        user.getRoles().add(role);
        User savedUser = userRepository.save(user);

        return ApiResponse.<UserResponse>builder()
                .success(true)
                .message("Role berhasil ditambahkan ke User")
                .data(mapToUserResponse(savedUser))
                .build();
    }

    @Override
    public ApiResponse<UserResponse> removeRoleFromUser(Long userId, String roleName) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User tidak ditemukan"));

        Role role = roleRepository.findByName(roleName.toUpperCase())
                .orElseThrow(() -> new ResourceNotFoundException("Role tidak ditemukan"));

        if(!user.getRoles().contains(role)){
            throw new BadRequestException("User tidak memiliki Role ini " + role.getName());
        }

        if(user.getRoles().size() == 1) {
            throw new BadRequestException("User minimal harus memiliki satu Role");
        }

        user.getRoles().remove(role);
        User savedUser = userRepository.save(user);

        return ApiResponse.<UserResponse>builder()
                .success(true)
                .message("Role berhasil dihapus dari User")
                .data(mapToUserResponse(savedUser))
                .build();
    }

    private UserResponse mapToUserResponse(User user) {
        Set<String> roles = user.getRoles()
                .stream()
                .map(Role::getName)
                .collect(Collectors.toSet());

        Set<String> permissions = user.getRoles()
                .stream()
                .flatMap(role -> role.getPermissions().stream())
                .map(Permission::getName)
                .collect(Collectors.toSet());

        return UserResponse.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .active(user.getActive())
                .roles(roles)
                .permissions(permissions)
                .build();
    }

    private RoleResponse mapToRoleResponse(Role role) {
        Set<String> permissions = role.getPermissions()
                .stream()
                .map(Permission::getName)
                .collect(Collectors.toSet());

        return RoleResponse.builder()
                .id(role.getId())
                .name(role.getName())
                .description(role.getDescription())
                .active(role.getActive())
                .permissions(permissions)
                .build();
    }

    private PermissionsResponse mapToPermissionsResponse(Permission permission) {
        return PermissionsResponse.builder()
                .id(permission.getId())
                .name(permission.getName())
                .description(permission.getDescription())
                .active(permission.getActive())
                .build();
    }
    
}
