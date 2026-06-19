package com.pangeranvalerensco.orchestria.notification_report_service.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

@Service
public class JwtService {

    @Value("${app.jwt.secret}")
    private String jwtSecret;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }

    public Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public List<String> extractRoles(String token) {
        return extractStringList(token, "roles");
    }

    public List<String> extractPermissions(String token) {
        return extractStringList(token, "permissions");
    }

    private List<String> extractStringList(String token, String claimName) {
        Object value = extractAllClaims(token).get(claimName);
        if (value instanceof List<?> values) {
            return values.stream().map(Object::toString).toList();
        }
        return List.of();
    }

    public boolean isTokenValid(String token) {
        try {
            Date expiration = extractAllClaims(token).getExpiration();
            return expiration != null && expiration.after(new Date());
        } catch (Exception exception) {
            return false;
        }
    }
}
