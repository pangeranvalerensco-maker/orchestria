package com.pangeranvalerensco.orchestria.auth_service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pangeranvalerensco.orchestria.auth_service.entity.OtpChallenge;
import com.pangeranvalerensco.orchestria.auth_service.entity.OtpPurpose;
import com.pangeranvalerensco.orchestria.auth_service.entity.Role;
import com.pangeranvalerensco.orchestria.auth_service.entity.User;
import com.pangeranvalerensco.orchestria.auth_service.payload.request.ForgotPasswordRequest;
import com.pangeranvalerensco.orchestria.auth_service.payload.request.ForgotPasswordVerifyRequest;
import com.pangeranvalerensco.orchestria.auth_service.payload.request.LoginRequest;
import com.pangeranvalerensco.orchestria.auth_service.payload.request.OtpVerifyRequest;
import com.pangeranvalerensco.orchestria.auth_service.repository.OtpChallengeRepository;
import com.pangeranvalerensco.orchestria.auth_service.repository.RoleRepository;
import com.pangeranvalerensco.orchestria.auth_service.repository.TrustedDeviceRepository;
import com.pangeranvalerensco.orchestria.auth_service.repository.UserRepository;
import com.pangeranvalerensco.orchestria.auth_service.security.JwtService;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.context.TestPropertySource;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(properties = "spring.main.allow-bean-definition-overriding=true")
public class AuthControllerMfaIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private OtpChallengeRepository otpChallengeRepository;

    @Autowired
    private TrustedDeviceRepository trustedDeviceRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private JwtService jwtService;

    @org.springframework.boot.test.context.TestConfiguration
    static class MockConfig {
        @org.springframework.context.annotation.Bean
        @org.springframework.context.annotation.Primary
        public org.springframework.web.client.RestTemplate restTemplate() {
            return org.mockito.Mockito.mock(org.springframework.web.client.RestTemplate.class);
        }
    }

    @Autowired
    private org.springframework.web.client.RestTemplate restTemplate;

    private User regularUser;
    private User superAdminUser;

    @BeforeEach
    void setUp() {
        otpChallengeRepository.deleteAll();
        trustedDeviceRepository.deleteAll();
        userRepository.deleteAll();
        roleRepository.deleteAll();

        Role anggotaRole = roleRepository.save(Role.builder().name("ANGGOTA").build());
        Role superAdminRole = roleRepository.save(Role.builder().name("SUPER_ADMIN").build());

        regularUser = userRepository.save(User.builder()
                .email("biasa@example.com")
                .password(passwordEncoder.encode("Password123!"))
                .fullName("Biasa")
                .active(true)
                .twoFactorEnabled(false)
                .roles(Set.of(anggotaRole))
                .build());

        superAdminUser = userRepository.save(User.builder()
                .email("admin@example.com")
                .password(passwordEncoder.encode("Password123!"))
                .fullName("Admin")
                .active(true)
                .twoFactorEnabled(false)
                .roles(Set.of(superAdminRole))
                .build());
    }

    // --- A. SAMAKAN NAMA COOKIE TRUSTED DEVICE ---

    @Test
    void testTrustedDeviceCookieLifecycle() throws Exception {
        // 1. verify OTP dengan rememberDevice menghasilkan Set-Cookie bernama ORCHESTRIA_TRUSTED_DEVICE
        String code = "123456";
        OtpChallenge challenge = otpChallengeRepository.save(OtpChallenge.builder()
                .id(UUID.randomUUID().toString())
                .userId(regularUser.getId())
                .purpose(OtpPurpose.LOGIN)
                .codeHash(passwordEncoder.encode(code))
                .expiresAt(LocalDateTime.now().plusMinutes(5))
                .resendAvailableAt(LocalDateTime.now())
                .build());

        OtpVerifyRequest verifyRequest = new OtpVerifyRequest();
        verifyRequest.setChallengeId(challenge.getId());
        verifyRequest.setCode(code);
        verifyRequest.setRememberDevice(true);
        verifyRequest.setDeviceName("My Test Device");

        String cookieHeader = mockMvc.perform(post("/api/auth/otp/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(verifyRequest)))
                .andExpect(status().isOk())
                .andExpect(cookie().exists("ORCHESTRIA_TRUSTED_DEVICE"))
                .andReturn().getResponse().getCookie("ORCHESTRIA_TRUSTED_DEVICE").getValue();

        // 2. login berikutnya dengan cookie tersebut meneruskan token mentah ke AuthService
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("biasa@example.com");
        loginRequest.setPassword("Password123!");

        regularUser.setTwoFactorEnabled(true);
        userRepository.save(regularUser);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest))
                        .cookie(new Cookie("ORCHESTRIA_TRUSTED_DEVICE", cookieHeader)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status", is("AUTHENTICATED")));

        // 3. logout membaca dan membersihkan cookie yang sama
        String jwtToken = jwtService.generateToken(regularUser);
        mockMvc.perform(post("/api/auth/logout")
                        .header("Authorization", "Bearer " + jwtToken)
                        .cookie(new Cookie("ORCHESTRIA_TRUSTED_DEVICE", cookieHeader)))
                .andExpect(status().isOk())
                .andExpect(cookie().value("ORCHESTRIA_TRUSTED_DEVICE", ""));

        // 4. pastikan string orchestria_device tidak tersisa.
        // There should be no Set-Cookie for 'orchestria_device'
        String setCookieHeader = mockMvc.perform(post("/api/auth/logout")
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andReturn().getResponse().getHeader("Set-Cookie");
        
        if (setCookieHeader != null) {
            assertFalse(setCookieHeader.contains("orchestria_device"));
        }
    }

    // --- B. BUAT FORGOT PASSWORD RESPONSE BERTIPE ---

    @Test
    void testForgotPasswordResponseFormat() throws Exception {
        ForgotPasswordRequest request = new ForgotPasswordRequest();
        request.setEmail("biasa@example.com");

        // 1. existing email mengembalikan object, bukan string
        // 3. JSON response memiliki: data.challengeId, data.expiresInSeconds, data.resendAfterSeconds
        mockMvc.perform(post("/api/auth/password/forgot")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.challengeId", notNullValue()))
                .andExpect(jsonPath("$.data.expiresInSeconds", is(300)))
                .andExpect(jsonPath("$.data.resendAfterSeconds", is(60)));

        // 2. non-existing email mengembalikan object dengan field yang sama
        ForgotPasswordRequest decoyRequest = new ForgotPasswordRequest();
        decoyRequest.setEmail("nonexistent@example.com");

        // 4. response existing dan non-existing memiliki struktur identik
        String decoyChallengeId = mockMvc.perform(post("/api/auth/password/forgot")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(decoyRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.challengeId", notNullValue()))
                .andExpect(jsonPath("$.data.expiresInSeconds", is(300)))
                .andExpect(jsonPath("$.data.resendAfterSeconds", is(60)))
                .andReturn().getResponse().getContentAsString();

        // extract challengeId from decoy response
        String challengeId = objectMapper.readTree(decoyChallengeId).get("data").get("challengeId").asText();

        // 5. decoy challenge pada verify menghasilkan pesan generik.
        // Actually, verify with invalid challenge will just throw BadRequestException.
        ForgotPasswordVerifyRequest verifyRequest = new ForgotPasswordVerifyRequest();
        verifyRequest.setChallengeId(challengeId);
        verifyRequest.setCode("123456");

        mockMvc.perform(post("/api/auth/password/forgot/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(verifyRequest)))
                .andExpect(status().isBadRequest());
    }

    // --- C. PERBAIKI MANDATORY MFA ---

    @Test
    void testMandatoryMfaForRoles() throws Exception {
        // 1. SUPER_ADMIN tidak dapat request disable
        String adminToken = jwtService.generateToken(superAdminUser);
        mockMvc.perform(post("/api/auth/2fa/disable/request")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message", is("Role Anda mewajibkan 2FA")));

        // 2. SUPER_ADMIN tidak dapat confirm disable
        OtpVerifyRequest dummyVerify = new OtpVerifyRequest();
        dummyVerify.setChallengeId("dummy");
        dummyVerify.setCode("123456");
        mockMvc.perform(post("/api/auth/2fa/disable/confirm")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dummyVerify))
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message", is("Role Anda mewajibkan 2FA")));

        // 3. privileged user dengan twoFactorEnabled=true tetap tidak dapat disable
        superAdminUser.setTwoFactorEnabled(true);
        userRepository.save(superAdminUser);
        mockMvc.perform(post("/api/auth/2fa/disable/request")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isForbidden());

        // 4. user biasa dengan twoFactorEnabled=true dapat disable
        regularUser.setTwoFactorEnabled(true);
        userRepository.save(regularUser);
        String regularToken = jwtService.generateToken(regularUser);
        mockMvc.perform(post("/api/auth/2fa/disable/request")
                        .header("Authorization", "Bearer " + regularToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("OTP dikirim")));

        // 5. mandatoryByRole true untuk seluruh privileged role
        mockMvc.perform(get("/api/auth/security")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.mandatoryByRole", is(true)));

        // 6. mandatoryByRole false untuk ANGGOTA
        mockMvc.perform(get("/api/auth/security")
                        .header("Authorization", "Bearer " + regularToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.mandatoryByRole", is(false)));
    }
}
