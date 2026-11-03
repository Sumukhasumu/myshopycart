package com.myshopycart.authservice;

import com.myshopycart.authservice.security.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    @Test
    void shouldGenerateAndValidateToken() {

        JwtService jwtService = new JwtService();

        ReflectionTestUtils.setField(
                jwtService,
                "jwtSecret",
                "myshopycart-phase4-jwt-secret-key-change-this-later"
        );

        ReflectionTestUtils.setField(
                jwtService,
                "jwtExpiration",
                3600000L
        );

        String token = jwtService.generateToken(
                2L,
                "sumukha",
                "USER"
        );

        assertNotNull(token);
        assertTrue(jwtService.isTokenValid(token));
        assertEquals("sumukha", jwtService.extractUsername(token));
        assertEquals(2L, jwtService.extractUserId(token));
        assertEquals("USER", jwtService.extractRole(token));
    }
}