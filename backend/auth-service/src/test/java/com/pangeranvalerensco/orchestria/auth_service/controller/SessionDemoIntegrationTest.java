package com.pangeranvalerensco.orchestria.auth_service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pangeranvalerensco.orchestria.auth_service.entity.Role;
import com.pangeranvalerensco.orchestria.auth_service.entity.User;
import com.pangeranvalerensco.orchestria.auth_service.payload.request.SessionDemoLoginRequest;
import com.pangeranvalerensco.orchestria.auth_service.repository.RoleRepository;
import com.pangeranvalerensco.orchestria.auth_service.repository.UserRepository;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Set;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class SessionDemoIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private ObjectMapper objectMapper = new ObjectMapper();

    private User activeUser;
    private User inactiveUser;
    private String rawPassword = "password123";

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        roleRepository.deleteAll();

        Role userRole = Role.builder().name("ROLE_USER").build();
        roleRepository.save(userRole);

        activeUser = User.builder()
                .fullName("Active User")
                .email("active@example.com")
                .password(passwordEncoder.encode(rawPassword))
                .roles(Set.of(userRole))
                .active(true)
                .build();
        userRepository.save(activeUser);

        inactiveUser = User.builder()
                .fullName("Inactive User")
                .email("inactive@example.com")
                .password(passwordEncoder.encode(rawPassword))
                .roles(Set.of(userRole))
                .active(false)
                .build();
        userRepository.save(inactiveUser);
    }

    @Test
    void testLoginValid() throws Exception {
        SessionDemoLoginRequest request = new SessionDemoLoginRequest();
        request.setEmail("active@example.com");
        request.setPassword(rawPassword);

        MvcResult result = mockMvc.perform(post("/api/auth/session-demo/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.authenticated", is(true)))
                .andExpect(jsonPath("$.data.user.email", is("active@example.com")))
                .andExpect(jsonPath("$.data.authenticationMode", is("STATEFUL_HTTP_SESSION")))
                .andExpect(jsonPath("$.data.accessToken").doesNotExist())
                .andExpect(jsonPath("$.data.sessionId").doesNotExist())
                .andExpect(header().string("Cache-Control", "no-store"))
                .andReturn();

        MockHttpSession session = (MockHttpSession) result.getRequest().getSession(false);
        assertNotNull(session);
        assertEquals(activeUser.getId(), session.getAttribute("SESSION_DEMO_USER_ID"));
        assertNotNull(session.getAttribute("SESSION_DEMO_AUTHENTICATED_AT"));
        assertEquals(900, session.getMaxInactiveInterval());
    }

    @Test
    void testLoginInvalidPassword() throws Exception {
        SessionDemoLoginRequest request = new SessionDemoLoginRequest();
        request.setEmail("active@example.com");
        request.setPassword("wrongpassword");

        mockMvc.perform(post("/api/auth/session-demo/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message", is("Email atau password salah")));
    }

    @Test
    void testLoginInactiveAccount() throws Exception {
        SessionDemoLoginRequest request = new SessionDemoLoginRequest();
        request.setEmail("inactive@example.com");
        request.setPassword(rawPassword);

        mockMvc.perform(post("/api/auth/session-demo/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message", is("Akun tidak aktif")));
    }

    @Test
    void testGetProfileWithoutSession() throws Exception {
        mockMvc.perform(get("/api/auth/session-demo/profile"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message", is("Sesi tidak valid atau telah berakhir")));
    }

    @Test
    void testGetProfileWithValidSession() throws Exception {
        SessionDemoLoginRequest request = new SessionDemoLoginRequest();
        request.setEmail("active@example.com");
        request.setPassword(rawPassword);

        MvcResult loginResult = mockMvc.perform(post("/api/auth/session-demo/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn();

        MockHttpSession session = (MockHttpSession) loginResult.getRequest().getSession(false);

        mockMvc.perform(get("/api/auth/session-demo/profile").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.authenticated", is(true)))
                .andExpect(jsonPath("$.data.user.email", is("active@example.com")))
                .andExpect(jsonPath("$.data.authenticationMode", is("STATEFUL_HTTP_SESSION")));
    }

    @Test
    void testStatusWithoutSession() throws Exception {
        mockMvc.perform(get("/api/auth/session-demo/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.authenticated", is(false)));
    }

    @Test
    void testStatusWithValidSession() throws Exception {
        SessionDemoLoginRequest request = new SessionDemoLoginRequest();
        request.setEmail("active@example.com");
        request.setPassword(rawPassword);

        MvcResult loginResult = mockMvc.perform(post("/api/auth/session-demo/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn();

        MockHttpSession session = (MockHttpSession) loginResult.getRequest().getSession(false);

        mockMvc.perform(get("/api/auth/session-demo/status").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.authenticated", is(true)));
    }

    @Test
    void testLogoutWithoutSession() throws Exception {
        mockMvc.perform(post("/api/auth/session-demo/logout"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.authenticated", is(false)))
                .andExpect(cookie().maxAge("ORCHESTRIA_SESSION_DEMO", 0));
    }

    @Test
    void testLogoutWithSession() throws Exception {
        SessionDemoLoginRequest request = new SessionDemoLoginRequest();
        request.setEmail("active@example.com");
        request.setPassword(rawPassword);

        MvcResult loginResult = mockMvc.perform(post("/api/auth/session-demo/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn();

        MockHttpSession session = (MockHttpSession) loginResult.getRequest().getSession(false);

        mockMvc.perform(post("/api/auth/session-demo/logout").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.authenticated", is(false)))
                .andExpect(cookie().maxAge("ORCHESTRIA_SESSION_DEMO", 0));

        assertTrue(session.isInvalid());
    }

    @Test
    void testProfileAfterLogout() throws Exception {
        SessionDemoLoginRequest request = new SessionDemoLoginRequest();
        request.setEmail("active@example.com");
        request.setPassword(rawPassword);

        MvcResult loginResult = mockMvc.perform(post("/api/auth/session-demo/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn();

        MockHttpSession session = (MockHttpSession) loginResult.getRequest().getSession(false);

        mockMvc.perform(post("/api/auth/session-demo/logout").session(session))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/auth/session-demo/profile").session(session))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testSecondLoginInvalidatesOldSession() throws Exception {
        SessionDemoLoginRequest request = new SessionDemoLoginRequest();
        request.setEmail("active@example.com");
        request.setPassword(rawPassword);

        MvcResult login1 = mockMvc.perform(post("/api/auth/session-demo/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn();

        MockHttpSession session1 = (MockHttpSession) login1.getRequest().getSession(false);

        MvcResult login2 = mockMvc.perform(post("/api/auth/session-demo/login")
                        .session(session1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn();

        assertTrue(session1.isInvalid());
        MockHttpSession session2 = (MockHttpSession) login2.getRequest().getSession(false);
        assertNotSame(session1, session2);
    }

    @Test
    void testUserDisabledAfterLoginLosesProfileAccess() throws Exception {
        SessionDemoLoginRequest request = new SessionDemoLoginRequest();
        request.setEmail("active@example.com");
        request.setPassword(rawPassword);

        MvcResult loginResult = mockMvc.perform(post("/api/auth/session-demo/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn();

        MockHttpSession session = (MockHttpSession) loginResult.getRequest().getSession(false);

        activeUser.setActive(false);
        userRepository.save(activeUser);

        mockMvc.perform(get("/api/auth/session-demo/profile").session(session))
                .andExpect(status().isUnauthorized());

        assertTrue(session.isInvalid());
    }

    @Test
    void testJwtCannotAccessSessionDemoProfile() throws Exception {
        // Assume we have a valid JWT token
        mockMvc.perform(get("/api/auth/session-demo/profile")
                        .header("Authorization", "Bearer dummy.jwt.token"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testSessionDemoCookieCannotAccessMeEndpoint() throws Exception {
        SessionDemoLoginRequest request = new SessionDemoLoginRequest();
        request.setEmail("active@example.com");
        request.setPassword(rawPassword);

        MvcResult loginResult = mockMvc.perform(post("/api/auth/session-demo/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn();

        MockHttpSession session = (MockHttpSession) loginResult.getRequest().getSession(false);

        // SecurityConfig is STATELESS for /api/auth/me, so it will ignore session
        mockMvc.perform(get("/api/auth/me").session(session))
                .andExpect(status().isUnauthorized());
    }
}
