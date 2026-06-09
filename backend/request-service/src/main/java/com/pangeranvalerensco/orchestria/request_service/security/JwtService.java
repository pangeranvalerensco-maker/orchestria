package com.pangeranvalerensco.orchestria.request_service.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.List;

@Service
public class JwtService {
    
    @Value("${app.jwt.secret}")
    private String jwtSecret;

    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }

    public Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith((javax.crypto.SecretKey) getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public String extracEmail(String token) {
        return extractAllClaims(token).getSubject();
    }

    public List<String> extractRoles(String token){
        Object roles = extractAllClaims(token).get("roles");

        if (roles instanceof List<?> roleList) {
            return roleList.stream()
                    .map(Object::toString)
                    .toList();
        }

        return List.of();
    }

    public List<String> extractPermissions(String token){
        Object permissions = extractAllClaims(token).get("permissions");

        if (permissions instanceof List<?> permissionList) {
            return permissionList.stream()
                    .map(Object::toString)
                    .toList();
        }

        return List.of();
    }

    public boolean isTokenValid(String token) {
        try {
            Date expiration = extractAllClaims(token).getExpiration();
            return expiration.after(new Date());
        } catch (Exception e) {
            return false;
        }
    }
}
