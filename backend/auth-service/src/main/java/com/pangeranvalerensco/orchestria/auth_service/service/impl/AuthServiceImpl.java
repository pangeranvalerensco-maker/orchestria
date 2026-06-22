package com.pangeranvalerensco.orchestria.auth_service.service.impl;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pangeranvalerensco.orchestria.auth_service.client.NotificationEmailClient;
import com.pangeranvalerensco.orchestria.auth_service.entity.*;
import com.pangeranvalerensco.orchestria.auth_service.exception.BadRequestException;
import com.pangeranvalerensco.orchestria.auth_service.exception.ForbiddenException;
import com.pangeranvalerensco.orchestria.auth_service.exception.ResourceNotFoundException;
import com.pangeranvalerensco.orchestria.auth_service.exception.UnauthorizedException;
import com.pangeranvalerensco.orchestria.auth_service.payload.request.*;
import com.pangeranvalerensco.orchestria.auth_service.payload.response.*;
import com.pangeranvalerensco.orchestria.auth_service.repository.*;
import com.pangeranvalerensco.orchestria.auth_service.security.JwtService;
import com.pangeranvalerensco.orchestria.auth_service.service.AuthService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final OtpChallengeRepository otpChallengeRepository;
    private final TrustedDeviceRepository trustedDeviceRepository;
    private final PasswordResetGrantRepository passwordResetGrantRepository;
    private final NotificationEmailClient notificationEmailClient;

    private final SecureRandom secureRandom = new SecureRandom();

    @Value("${app.auth.trusted-device.days:7}")
    private int trustedDeviceDays;

    @Override
    public ApiResponse<UserResponse> register(RegisterRequest request) {
        String normalizedEmail = request.getEmail().trim().toLowerCase();

        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new BadRequestException("Email sudah terdaftar");
        }

        Role defaultRole = roleRepository.findByName("ANGGOTA")
                .orElseThrow(() -> new ResourceNotFoundException("Role default Anggota tidak ditemukan"));

        User user = User.builder()
                .fullName(request.getFullName())
                .email(normalizedEmail)
                .password(passwordEncoder.encode(request.getPassword()))
                .active(true)
                .twoFactorEnabled(false)
                .roles(Set.of(defaultRole))
                .build();

        User savedUser = userRepository.save(user);

        return ApiResponse.<UserResponse>builder()
                .success(true)
                .message("Registrasi berhasil")
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

        boolean isPrivileged = isMandatoryByRole(user);

        return UserResponse.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .active(user.getActive())
                .roles(roles)
                .permissions(permissions)
                .twoFactorEnabled(user.getTwoFactorEnabled())
                .twoFactorRequired(isPrivileged || user.getTwoFactorEnabled())
                .build();
    }

    @Override
    @Transactional
    public ApiResponse<LoginResult> login(LoginRequest request, String userAgent, String ipAddress, String trustedDeviceToken) {
        User user = userRepository.findByEmail(request.getEmail().trim().toLowerCase())
                .orElseThrow(() -> new UnauthorizedException("Email atau password salah"));

        if (Boolean.FALSE.equals(user.getActive())) {
            throw new UnauthorizedException("Email atau password salah"); // generic per requirements
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new UnauthorizedException("Email atau password salah");
        }

        UserResponse userResponse = mapToUserResponse(user);
        
        if (userResponse.getTwoFactorRequired()) {
            if (trustedDeviceToken != null && !trustedDeviceToken.isEmpty()) {
                String hash = sha256Hash(trustedDeviceToken);
                var deviceOpt = trustedDeviceRepository.findByUserIdAndTokenHash(user.getId(), hash);
                
                if (deviceOpt.isPresent()) {
                    TrustedDevice device = deviceOpt.get();
                    if (device.getRevokedAt() == null && device.getExpiresAt().isAfter(LocalDateTime.now())) {
                        device.setLastUsedAt(LocalDateTime.now());
                        device.setLastIpAddress(ipAddress);
                        trustedDeviceRepository.save(device);
                        
                        return buildAuthenticatedResult(user);
                    }
                }
            }
            
            // Require OTP
            otpChallengeRepository.invalidateActiveChallenges(user.getId(), OtpPurpose.LOGIN, LocalDateTime.now());
            
            String code = generateNumericOtp();
            OtpChallenge challenge = createChallenge(user.getId(), OtpPurpose.LOGIN, code);
            
            notificationEmailClient.sendEmail(user.getEmail(), "Kode verifikasi login Orchestria", 
                "Kode OTP login Anda: " + code + "\n\nBerlaku 5 menit. Jangan bagikan kode ini.");

            LoginResult result = LoginResult.builder()
                .status(LoginStatus.OTP_REQUIRED)
                .challengeId(challenge.getId())
                .maskedEmail(maskEmail(user.getEmail()))
                .expiresInSeconds(300)
                .resendAfterSeconds(60)
                .build();

            return ApiResponse.<LoginResult>builder().success(true).message("OTP dikirim").data(result).build();
        }

        return buildAuthenticatedResult(user);
    }

    private ApiResponse<LoginResult> buildAuthenticatedResult(User user) {
        String token = jwtService.generateToken(user);
        AuthResponse authResponse = AuthResponse.builder()
                .tokenType("Bearer")
                .accessToken(token)
                .user(mapToUserResponse(user))
                .build();

        LoginResult result = LoginResult.builder()
                .status(LoginStatus.AUTHENTICATED)
                .authData(authResponse)
                .build();

        return ApiResponse.<LoginResult>builder()
                .success(true)
                .message("Login berhasil")
                .data(result)
                .build();
    }

    @Override
    @Transactional
    public ApiResponse<AuthResponse> verifyOtp(OtpVerifyRequest request, String userAgent, String ipAddress) {
        OtpChallenge challenge = validateAndConsumeOtp(request.getChallengeId(), request.getCode(), OtpPurpose.LOGIN);
        User user = userRepository.findById(challenge.getUserId())
            .orElseThrow(() -> new UnauthorizedException("User tidak ditemukan"));
            
        String token = jwtService.generateToken(user);
        AuthResponse authResponse = AuthResponse.builder()
                .tokenType("Bearer")
                .accessToken(token)
                .user(mapToUserResponse(user))
                .build();
                
        // trusted device logic is handled in the controller to set the cookie
        return ApiResponse.<AuthResponse>builder().success(true).message("Login OTP berhasil").data(authResponse).build();
    }

    @Override
    @Transactional
    public ApiResponse<OtpResendResponse> resendOtp(OtpResendRequest request) {
        OtpChallenge challenge = otpChallengeRepository.findById(request.getChallengeId())
            .orElseThrow(() -> new BadRequestException("Challenge tidak valid"));
            
        if (challenge.getConsumedAt() != null || challenge.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Challenge tidak valid atau kadaluarsa");
        }
        
        if (challenge.getResendCount() >= 5) {
            throw new BadRequestException("Batas resend tercapai");
        }
        
        if (challenge.getResendAvailableAt().isAfter(LocalDateTime.now())) {
            throw new BadRequestException("Harap tunggu sebelum mengirim ulang");
        }
        
        String code = generateNumericOtp();
        challenge.setCodeHash(passwordEncoder.encode(code));
        challenge.setExpiresAt(LocalDateTime.now().plusMinutes(5));
        challenge.setResendCount(challenge.getResendCount() + 1);
        challenge.setResendAvailableAt(LocalDateTime.now().plusSeconds(60));
        
        otpChallengeRepository.save(challenge);
        
        User user = userRepository.findById(challenge.getUserId()).orElseThrow();
        String subject = switch(challenge.getPurpose()) {
            case LOGIN -> "Kode verifikasi login Orchestria";
            case FORGOT_PASSWORD -> "Kode reset password Orchestria";
            case ENABLE_2FA -> "Kode konfirmasi keamanan Orchestria";
            case DISABLE_2FA -> "Kode konfirmasi keamanan Orchestria";
        };
        
        notificationEmailClient.sendEmail(user.getEmail(), subject, 
            "Kode OTP Anda: " + code + "\n\nBerlaku 5 menit. Jangan bagikan kode ini.");
            
        OtpResendResponse response = OtpResendResponse.builder()
            .expiresInSeconds(300)
            .resendAfterSeconds(60)
            .build();
            
        return ApiResponse.<OtpResendResponse>builder().success(true).message("OTP dikirim ulang").data(response).build();
    }

    @Override
    @Transactional
    public ApiResponse<ForgotPasswordStartResponse> forgotPassword(ForgotPasswordRequest request) {
        var userOpt = userRepository.findByEmail(request.getEmail().trim().toLowerCase());
        
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            otpChallengeRepository.invalidateActiveChallenges(user.getId(), OtpPurpose.FORGOT_PASSWORD, LocalDateTime.now());
            String code = generateNumericOtp();
            OtpChallenge challenge = createChallenge(user.getId(), OtpPurpose.FORGOT_PASSWORD, code);
            notificationEmailClient.sendEmail(user.getEmail(), "Kode reset password Orchestria", 
                "Kode OTP reset password: " + code + "\n\nBerlaku 5 menit. Jangan bagikan.");
            
            ForgotPasswordStartResponse responseData = ForgotPasswordStartResponse.builder()
                .challengeId(challenge.getId())
                .expiresInSeconds(300)
                .resendAfterSeconds(60)
                .build();
                
            return ApiResponse.<ForgotPasswordStartResponse>builder().success(true).message("Jika akun tersedia, instruksi reset password telah dikirim.")
                .data(responseData).build();
        }
        
        // Decoy logic for anti-enumeration
        String decoyId = UUID.randomUUID().toString();
        ForgotPasswordStartResponse decoyData = ForgotPasswordStartResponse.builder()
            .challengeId(decoyId)
            .expiresInSeconds(300)
            .resendAfterSeconds(60)
            .build();
            
        return ApiResponse.<ForgotPasswordStartResponse>builder().success(true).message("Jika akun tersedia, instruksi reset password telah dikirim.")
            .data(decoyData).build();
    }

    @Override
    @Transactional
    public ApiResponse<ForgotPasswordVerifyResponse> verifyForgotPassword(ForgotPasswordVerifyRequest request) {
        OtpChallenge challenge = validateAndConsumeOtp(request.getChallengeId(), request.getCode(), OtpPurpose.FORGOT_PASSWORD);
        
        String rawToken = generateRandomToken();
        PasswordResetGrant grant = PasswordResetGrant.builder()
            .id(UUID.randomUUID().toString())
            .userId(challenge.getUserId())
            .tokenHash(sha256Hash(rawToken))
            .expiresAt(LocalDateTime.now().plusMinutes(10))
            .build();
            
        passwordResetGrantRepository.save(grant);
        
        ForgotPasswordVerifyResponse response = ForgotPasswordVerifyResponse.builder()
            .resetToken(rawToken)
            .expiresInSeconds(600)
            .build();
            
        return ApiResponse.<ForgotPasswordVerifyResponse>builder().success(true).message("Verifikasi berhasil").data(response).build();
    }

    @Override
    @Transactional
    public ApiResponse<String> resetPassword(PasswordResetRequest request) {
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new BadRequestException("Konfirmasi password tidak cocok");
        }
        
        if (!request.getNewPassword().matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).{8,}$")) {
            throw new BadRequestException("Password minimal 8 karakter, mengandung huruf besar, huruf kecil, dan angka");
        }
        
        String hash = sha256Hash(request.getResetToken());
        PasswordResetGrant grant = passwordResetGrantRepository.findByTokenHash(hash)
            .filter(g -> g.getUsedAt() == null && g.getExpiresAt().isAfter(LocalDateTime.now()))
            .orElseThrow(() -> new BadRequestException("Token reset password tidak valid atau sudah kadaluarsa"));
            
        User user = userRepository.findById(grant.getUserId()).orElseThrow();
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        
        grant.setUsedAt(LocalDateTime.now());
        passwordResetGrantRepository.save(grant);
        
        passwordResetGrantRepository.invalidateAllActiveGrants(user.getId(), LocalDateTime.now());
        trustedDeviceRepository.revokeAllByUserId(user.getId(), LocalDateTime.now());
        otpChallengeRepository.invalidateAllActiveChallenges(user.getId(), LocalDateTime.now());
        
        return ApiResponse.<String>builder().success(true).message("Password berhasil direset").build();
    }

    @Override
    public ApiResponse<SecuritySettings> getSecuritySettings(String email) {
        User user = userRepository.findByEmail(email).orElseThrow();
        UserResponse ur = mapToUserResponse(user);
        
        int deviceCount = trustedDeviceRepository.findAllByUserIdAndRevokedAtIsNull(user.getId()).size();
        
        boolean mandatoryByRole = isMandatoryByRole(user);
        
        SecuritySettings settings = SecuritySettings.builder()
            .twoFactorEnabled(ur.getTwoFactorEnabled())
            .twoFactorRequired(mandatoryByRole || Boolean.TRUE.equals(user.getTwoFactorEnabled()))
            .mandatoryByRole(mandatoryByRole)
            .trustedDeviceCount(deviceCount)
            .build();
            
        return ApiResponse.<SecuritySettings>builder().success(true).data(settings).build();
    }

    @Override
    @Transactional
    public ApiResponse<String> requestEnableTwoFactor(String email) {
        User user = userRepository.findByEmail(email).orElseThrow();
        if (user.getTwoFactorEnabled()) {
            throw new BadRequestException("2FA sudah aktif");
        }
        
        otpChallengeRepository.invalidateActiveChallenges(user.getId(), OtpPurpose.ENABLE_2FA, LocalDateTime.now());
        String code = generateNumericOtp();
        OtpChallenge challenge = createChallenge(user.getId(), OtpPurpose.ENABLE_2FA, code);
        
        notificationEmailClient.sendEmail(user.getEmail(), "Kode konfirmasi keamanan Orchestria", 
            "Kode OTP konfirmasi keamanan Anda: " + code + "\n\nBerlaku 5 menit.");
            
        return ApiResponse.<String>builder().success(true).message("OTP dikirim").data(challenge.getId()).build();
    }

    @Override
    @Transactional
    public ApiResponse<String> confirmEnableTwoFactor(String email, OtpVerifyRequest request) {
        User user = userRepository.findByEmail(email).orElseThrow();
        OtpChallenge challenge = validateAndConsumeOtp(request.getChallengeId(), request.getCode(), OtpPurpose.ENABLE_2FA);
        
        if (!challenge.getUserId().equals(user.getId())) {
            throw new ForbiddenException("Invalid user");
        }
        
        user.setTwoFactorEnabled(true);
        userRepository.save(user);
        
        return ApiResponse.<String>builder().success(true).message("2FA berhasil diaktifkan").build();
    }

    @Override
    @Transactional
    public ApiResponse<String> requestDisableTwoFactor(String email) {
        User user = userRepository.findByEmail(email).orElseThrow();
        if (isMandatoryByRole(user)) {
            throw new ForbiddenException("Role Anda mewajibkan 2FA");
        }
        
        otpChallengeRepository.invalidateActiveChallenges(user.getId(), OtpPurpose.DISABLE_2FA, LocalDateTime.now());
        String code = generateNumericOtp();
        OtpChallenge challenge = createChallenge(user.getId(), OtpPurpose.DISABLE_2FA, code);
        
        notificationEmailClient.sendEmail(user.getEmail(), "Kode konfirmasi keamanan Orchestria", 
            "Kode OTP konfirmasi keamanan Anda: " + code + "\n\nBerlaku 5 menit.");
            
        return ApiResponse.<String>builder().success(true).message("OTP dikirim").data(challenge.getId()).build();
    }

    @Override
    @Transactional
    public ApiResponse<String> confirmDisableTwoFactor(String email, OtpVerifyRequest request) {
        User user = userRepository.findByEmail(email).orElseThrow();
        if (isMandatoryByRole(user)) {
            throw new ForbiddenException("Role Anda mewajibkan 2FA");
        }
        
        OtpChallenge challenge = validateAndConsumeOtp(request.getChallengeId(), request.getCode(), OtpPurpose.DISABLE_2FA);
        
        if (!challenge.getUserId().equals(user.getId())) {
            throw new ForbiddenException("Invalid user");
        }
        
        user.setTwoFactorEnabled(false);
        userRepository.save(user);
        trustedDeviceRepository.revokeAllByUserId(user.getId(), LocalDateTime.now());
        
        return ApiResponse.<String>builder().success(true).message("2FA berhasil dinonaktifkan").build();
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
    
    // --- Trusted Device Management ---
    
    @Override
    @Transactional
    public String createTrustedDevice(Long userId, String deviceName, String userAgent, String ipAddress) {
        String rawToken = UUID.randomUUID().toString() + "-" + UUID.randomUUID().toString();
        String tokenHash = sha256Hash(rawToken);
        
        TrustedDevice device = TrustedDevice.builder()
            .id(UUID.randomUUID().toString())
            .userId(userId)
            .tokenHash(tokenHash)
            .deviceName(deviceName != null && !deviceName.isEmpty() ? deviceName : "Unknown Device")
            .userAgent(userAgent)
            .lastIpAddress(ipAddress)
            .expiresAt(LocalDateTime.now().plusDays(trustedDeviceDays))
            .build();
            
        trustedDeviceRepository.save(device);
        return rawToken;
    }

    @Override
    public ApiResponse<List<TrustedDeviceResponse>> getTrustedDevices(String email) {
        User user = userRepository.findByEmail(email).orElseThrow();
        List<TrustedDevice> devices = trustedDeviceRepository.findAllByUserIdAndRevokedAtIsNull(user.getId());
        List<TrustedDeviceResponse> responses = devices.stream().map(d -> TrustedDeviceResponse.builder()
                .id(d.getId())
                .deviceName(d.getDeviceName())
                .userAgent(d.getUserAgent())
                .lastIpAddress(d.getLastIpAddress())
                .lastUsedAt(d.getLastUsedAt())
                .expiresAt(d.getExpiresAt())
                .createdAt(d.getCreatedAt())
                .build()).collect(Collectors.toList());
                
        return ApiResponse.<List<TrustedDeviceResponse>>builder().success(true).data(responses).build();
    }

    @Override
    @Transactional
    public ApiResponse<String> revokeTrustedDevice(String email, String id) {
        User user = userRepository.findByEmail(email).orElseThrow();
        TrustedDevice device = trustedDeviceRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Perangkat tidak ditemukan"));
            
        if (!device.getUserId().equals(user.getId())) {
            throw new ForbiddenException("Akses ditolak");
        }
        
        device.setRevokedAt(LocalDateTime.now());
        trustedDeviceRepository.save(device);
        
        return ApiResponse.<String>builder().success(true).message("Perangkat berhasil dihapus").build();
    }

    @Override
    @Transactional
    public ApiResponse<String> revokeAllTrustedDevices(String email) {
        User user = userRepository.findByEmail(email).orElseThrow();
        trustedDeviceRepository.revokeAllByUserId(user.getId(), LocalDateTime.now());
        return ApiResponse.<String>builder().success(true).message("Semua perangkat berhasil dihapus").build();
    }

    @Override
    @Transactional
    public ApiResponse<String> logout(String trustedDeviceToken) {
        if (trustedDeviceToken != null && !trustedDeviceToken.trim().isEmpty()) {
            String hash = sha256Hash(trustedDeviceToken);
            trustedDeviceRepository.findByTokenHash(hash)
                .filter(device -> device.getRevokedAt() == null)
                .ifPresent(device -> {
                    device.setRevokedAt(LocalDateTime.now());
                    trustedDeviceRepository.save(device);
                });
        }
        return ApiResponse.<String>builder().success(true).message("Logout berhasil").build();
    }
    
    // --- Helpers ---
    
    private boolean isMandatoryByRole(User user) {
        return user.getRoles().stream().anyMatch(role -> 
            role.getName().equals("SUPER_ADMIN") || 
            role.getName().equals("KETUA_PUB") || 
            role.getName().equals("PEMBINA") || 
            role.getName().equals("BENDAHARA_INTERNAL") || 
            role.getName().equals("BENDAHARA_EKSTERNAL")
        );
    }
    
    private OtpChallenge createChallenge(Long userId, OtpPurpose purpose, String code) {
        OtpChallenge challenge = OtpChallenge.builder()
            .id(UUID.randomUUID().toString())
            .userId(userId)
            .purpose(purpose)
            .codeHash(passwordEncoder.encode(code))
            .expiresAt(LocalDateTime.now().plusMinutes(5))
            .resendAvailableAt(LocalDateTime.now().plusSeconds(60))
            .build();
        return otpChallengeRepository.save(challenge);
    }
    
    private OtpChallenge validateAndConsumeOtp(String challengeId, String code, OtpPurpose purpose) {
        OtpChallenge challenge = otpChallengeRepository.findById(challengeId)
            .orElseThrow(() -> new BadRequestException("Kode verifikasi tidak valid atau kedaluwarsa"));
            
        if (challenge.getPurpose() != purpose) {
            throw new BadRequestException("Kode verifikasi tidak valid atau kedaluwarsa");
        }
        if (challenge.getConsumedAt() != null) {
            throw new BadRequestException("Kode verifikasi tidak valid atau kedaluwarsa");
        }
        if (challenge.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Kode verifikasi tidak valid atau kedaluwarsa");
        }
        if (challenge.getAttemptCount() >= challenge.getMaxAttempts()) {
            throw new BadRequestException("Kode verifikasi tidak valid atau kedaluwarsa");
        }
        
        if (!passwordEncoder.matches(code, challenge.getCodeHash())) {
            challenge.setAttemptCount(challenge.getAttemptCount() + 1);
            otpChallengeRepository.save(challenge);
            throw new BadRequestException("Kode verifikasi tidak valid atau kedaluwarsa");
        }
        
        challenge.setConsumedAt(LocalDateTime.now());
        return otpChallengeRepository.save(challenge);
    }

    private String generateNumericOtp() {
        int code = 100000 + secureRandom.nextInt(900000);
        return String.valueOf(code);
    }

    private String generateRandomToken() {
        byte[] randomBytes = new byte[32];
        secureRandom.nextBytes(randomBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    }

    public String sha256Hash(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] encodedhash = digest.digest(input.getBytes());
            StringBuilder hexString = new StringBuilder(2 * encodedhash.length);
            for (byte b : encodedhash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not supported", e);
        }
    }

    private String maskEmail(String email) {
        String[] parts = email.split("@");
        if (parts.length != 2) return email;
        String name = parts[0];
        if (name.length() <= 2) return name + "@" + parts[1];
        return name.substring(0, 2) + "****@" + parts[1];
    }
}
