package com.myshopycart.inventoryservice.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.List;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String INTERNAL_TOKEN_HEADER =
            "X-Internal-Service-Token";

    private final JwtService jwtService;
    private final String internalToken;

    public JwtAuthenticationFilter(
            JwtService jwtService,
            @Value("${myshopycart.inventory.internal-token:}")
            String internalToken) {

        this.jwtService = jwtService;
        this.internalToken = internalToken;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        String suppliedInternalToken =
                request.getHeader(INTERNAL_TOKEN_HEADER);

        if (isValidInternalToken(suppliedInternalToken)) {

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            "order-service",
                            null,
                            List.of(
                                    new SimpleGrantedAuthority(
                                            "ROLE_INVENTORY_SERVICE")
                            )
                    );

            SecurityContextHolder
                    .getContext()
                    .setAuthentication(authentication);

            filterChain.doFilter(request, response);
            return;
        }

        String authorizationHeader =
                request.getHeader("Authorization");

        if (authorizationHeader == null
                || !authorizationHeader.startsWith("Bearer ")) {

            filterChain.doFilter(request, response);
            return;
        }

        String token = authorizationHeader.substring(7);

        if (!jwtService.isTokenValid(token)) {
            filterChain.doFilter(request, response);
            return;
        }

        String username = jwtService.extractUsername(token);
        Long userId = jwtService.extractUserId(token);
        String role = jwtService.extractRole(token);

        if (role == null || role.isBlank()) {
            filterChain.doFilter(request, response);
            return;
        }

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        username,
                        null,
                        List.of(
                                new SimpleGrantedAuthority(
                                        "ROLE_" + role)
                        )
                );

        authentication.setDetails(userId);

        SecurityContextHolder
                .getContext()
                .setAuthentication(authentication);

        filterChain.doFilter(request, response);
    }

    private boolean isValidInternalToken(String suppliedToken) {

        if (internalToken == null || internalToken.isBlank()
                || suppliedToken == null || suppliedToken.isBlank()) {
            return false;
        }

        return MessageDigest.isEqual(
                internalToken.getBytes(StandardCharsets.UTF_8),
                suppliedToken.getBytes(StandardCharsets.UTF_8));
    }
}