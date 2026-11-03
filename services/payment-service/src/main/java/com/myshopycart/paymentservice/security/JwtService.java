package com.myshopycart.paymentservice.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

@Service
public class JwtService {

    private final String jwtSecret;

    public JwtService(
            @Value("${myshopycart.auth.jwt-secret}") String jwtSecret) {

        this.jwtSecret = jwtSecret;
    }

    private SecretKey getSigningKey() {

        return Keys.hmacShaKeyFor(
                jwtSecret.getBytes(StandardCharsets.UTF_8)
        );
    }

    public Claims extractAllClaims(String token) {

        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public String extractUsername(String token) {

        return extractAllClaims(token)
                .getSubject();
    }

    public Long extractUserId(String token) {

        Object userId = extractAllClaims(token)
                .get("userId");

        if (userId == null) {
            return null;
        }

        if (userId instanceof Number number) {
            return number.longValue();
        }

        return Long.valueOf(userId.toString());
    }

    public String extractRole(String token) {

        Object role = extractAllClaims(token)
                .get("role");

        return role != null
                ? role.toString()
                : null;
    }

    public boolean isTokenValid(String token) {

        try {
            extractAllClaims(token);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }
}