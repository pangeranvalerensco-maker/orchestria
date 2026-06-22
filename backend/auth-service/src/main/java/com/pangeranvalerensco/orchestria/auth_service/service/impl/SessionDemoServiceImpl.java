package com.pangeranvalerensco.orchestria.auth_service.service.impl;

import com.pangeranvalerensco.orchestria.auth_service.entity.User;
import com.pangeranvalerensco.orchestria.auth_service.exception.UnauthorizedException;
import com.pangeranvalerensco.orchestria.auth_service.payload.request.SessionDemoLoginRequest;
import com.pangeranvalerensco.orchestria.auth_service.payload.response.SessionDemoResponse;
import com.pangeranvalerensco.orchestria.auth_service.payload.response.SessionDemoUserResponse;
import com.pangeranvalerensco.orchestria.auth_service.repository.UserRepository;
import com.pangeranvalerensco.orchestria.auth_service.service.SessionDemoService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SessionDemoServiceImpl implements SessionDemoService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.session-demo.timeout-seconds:900}")
    private int sessionTimeoutSeconds;

    @Value("${app.session-demo.enabled:true}")
    private boolean sessionDemoEnabled;

    private static final String SESSION_USER_ID = "SESSION_DEMO_USER_ID";
    private static final String SESSION_AUTH_AT = "SESSION_DEMO_AUTHENTICATED_AT";

    private void checkEnabled() {
        if (!sessionDemoEnabled) {
            throw new UnauthorizedException("Session Demo feature is disabled");
        }
    }

    @Override
    public SessionDemoResponse login(SessionDemoLoginRequest request, HttpServletRequest httpRequest) {
        checkEnabled();

        // 1. Normalisasi email & 2. Cari user
        String email = request.getEmail().toLowerCase().trim();
        Optional<User> userOpt = userRepository.findByEmail(email);

        if (userOpt.isEmpty()) {
            throw new UnauthorizedException("Email atau password salah");
        }

        User user = userOpt.get();

        // 3. Validasi akun aktif
        if (Boolean.FALSE.equals(user.getActive())) {
            throw new UnauthorizedException("Akun tidak aktif");
        }

        // 4. Validasi password
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new UnauthorizedException("Email atau password salah");
        }

        // 6. Invalidate session lama jika ada (mencegah session fixation)
        HttpSession oldSession = httpRequest.getSession(false);
        if (oldSession != null) {
            oldSession.invalidate();
        }

        // 7. Buat session baru
        HttpSession newSession = httpRequest.getSession(true);

        // 8. Simpan atribut & 9. Set maxInactiveInterval
        newSession.setAttribute(SESSION_USER_ID, user.getId());
        LocalDateTime now = LocalDateTime.now();
        newSession.setAttribute(SESSION_AUTH_AT, now);
        newSession.setMaxInactiveInterval(sessionTimeoutSeconds);

        // 11. Return response
        return buildResponse(newSession, user, "Login stateful session berhasil");
    }

    @Override
    public SessionDemoResponse getProfile(HttpServletRequest httpRequest) {
        checkEnabled();

        HttpSession session = httpRequest.getSession(false);
        if (session == null || session.getAttribute(SESSION_USER_ID) == null) {
            throw new UnauthorizedException("Sesi tidak valid atau telah berakhir");
        }

        Long userId = (Long) session.getAttribute(SESSION_USER_ID);
        User user = userRepository.findById(userId).orElse(null);

        if (user == null || Boolean.FALSE.equals(user.getActive())) {
            session.invalidate();
            throw new UnauthorizedException("Pengguna tidak ditemukan atau tidak aktif");
        }

        return buildResponse(session, user, "Profil berhasil diambil dari session");
    }

    @Override
    public SessionDemoResponse getStatus(HttpServletRequest httpRequest) {
        checkEnabled();

        HttpSession session = httpRequest.getSession(false);
        if (session == null || session.getAttribute(SESSION_USER_ID) == null) {
            return SessionDemoResponse.builder()
                    .authenticated(false)
                    .message("Tidak ada sesi yang aktif")
                    .build();
        }

        Long userId = (Long) session.getAttribute(SESSION_USER_ID);
        User user = userRepository.findById(userId).orElse(null);

        if (user == null || Boolean.FALSE.equals(user.getActive())) {
            session.invalidate();
            return SessionDemoResponse.builder()
                    .authenticated(false)
                    .message("Sesi tidak valid")
                    .build();
        }

        return buildResponse(session, user, "Sesi aktif");
    }

    @Override
    public SessionDemoResponse logout(HttpServletRequest httpRequest) {
        checkEnabled();

        HttpSession session = httpRequest.getSession(false);
        if (session != null) {
            session.invalidate();
        }

        return SessionDemoResponse.builder()
                .authenticated(false)
                .message("Logout stateful session berhasil")
                .build();
    }

    private SessionDemoResponse buildResponse(HttpSession session, User user, String message) {
        LocalDateTime createdAt = (LocalDateTime) session.getAttribute(SESSION_AUTH_AT);
        LocalDateTime lastAccessedAt = LocalDateTime.ofInstant(Instant.ofEpochMilli(session.getLastAccessedTime()), ZoneId.systemDefault());
        
        long nowSeconds = Instant.now().getEpochSecond();
        long lastAccessedSeconds = session.getLastAccessedTime() / 1000;
        long expiresInSeconds = (lastAccessedSeconds + session.getMaxInactiveInterval()) - nowSeconds;
        if (expiresInSeconds < 0) expiresInSeconds = 0;

        SessionDemoUserResponse userResponse = SessionDemoUserResponse.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .roles(user.getRoles().stream()
                        .map(r -> r.getName())
                        .collect(Collectors.toList()))
                .build();

        return SessionDemoResponse.builder()
                .authenticated(true)
                .user(userResponse)
                .createdAt(createdAt)
                .lastAccessedAt(lastAccessedAt)
                .expiresInSeconds(expiresInSeconds)
                .message(message)
                .build();
    }
}
