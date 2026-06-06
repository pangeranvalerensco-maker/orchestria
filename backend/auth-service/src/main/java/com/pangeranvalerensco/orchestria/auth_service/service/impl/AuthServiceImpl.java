package com.pangeranvalerensco.orchestria.auth_service.service.impl;

import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.pangeranvalerensco.orchestria.auth_service.entity.Permission;
import com.pangeranvalerensco.orchestria.auth_service.entity.Role;
import com.pangeranvalerensco.orchestria.auth_service.entity.User;
import com.pangeranvalerensco.orchestria.auth_service.payload.request.RegisterRequest;
import com.pangeranvalerensco.orchestria.auth_service.payload.request.LoginRequest;
import com.pangeranvalerensco.orchestria.auth_service.payload.response.ApiResponse;
import com.pangeranvalerensco.orchestria.auth_service.payload.response.UserResponse;
import com.pangeranvalerensco.orchestria.auth_service.payload.response.AuthResponse;
import com.pangeranvalerensco.orchestria.auth_service.repository.RoleRepository;
import com.pangeranvalerensco.orchestria.auth_service.repository.UserRepository;
import com.pangeranvalerensco.orchestria.auth_service.service.AuthService;
import com.pangeranvalerensco.orchestria.auth_service.security.JwtService;
import com.pangeranvalerensco.orchestria.auth_service.exception.BadRequestException;
import com.pangeranvalerensco.orchestria.auth_service.exception.UnauthorizedException;
import com.pangeranvalerensco.orchestria.auth_service.exception.ResourceNotFoundException;
import com.pangeranvalerensco.orchestria.auth_service.exception.ForbiddenException;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Override
    public ApiResponse<UserResponse> register(RegisterRequest request) {
        if(userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email sudah terdaftar");
        }

        Role defaultRole = roleRepository.findByName("ANGGOTA")
                .orElseGet(() -> roleRepository.save(
                        Role.builder()
                                .name("ANGGOTA")
                                .description("Role default untuk Anggota")
                                .active(true)
                                .build()
                ));

        User user = User.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .active(true)
                .roles(Set.of(defaultRole))
                .build();
        
        User savedUser = userRepository.save(user);

        UserResponse userResponse = mapToUserResponse(savedUser);

        return ApiResponse.<UserResponse>builder()
                .success(true)
                .message("Registrasi berhasil")
                .data(userResponse)
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

    @Override
    public ApiResponse<AuthResponse> login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UnauthorizedException("Email atau password salah"));

        if(Boolean.FALSE.equals(user.getActive())) {
            throw new ForbiddenException("Akun tidak aktif");
        }

        boolean passwordMatch = passwordEncoder.matches(
            request.getPassword(), 
            user.getPassword()
        );

        if (!passwordMatch) {
            throw new UnauthorizedException("Email atau password salah");
        }

        String token = jwtService.generateToken(user);

        AuthResponse authResponse = AuthResponse.builder()
                .tokenType("Bearer")
                .accessToken(token)
                .user(mapToUserResponse(user))
                .build();

        return ApiResponse.<AuthResponse>builder()
                .success(true)
                .message("Login berhasil")
                .data(authResponse)
                .build();
    }

    @Override
    public ApiResponse<UserResponse> getCurrentUser(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User tidak ditemukan"));

        return ApiResponse.<UserResponse>builder()
                .success(true)
                .message("Data User berhasil diambil")
                .data(mapToUserResponse(user))
                .build();
    }
}
