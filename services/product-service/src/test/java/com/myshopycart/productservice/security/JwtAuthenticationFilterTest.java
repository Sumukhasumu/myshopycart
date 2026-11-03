package com.myshopycart.productservice.security;

import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class JwtAuthenticationFilterTest {

    private final JwtService jwtService = mock(JwtService.class);

    private final JwtAuthenticationFilter filter =
            new JwtAuthenticationFilter(jwtService);

    @BeforeEach
    void clearSecurityContextBeforeTest() {
    SecurityContextHolder.clearContext();
    }

   @AfterEach
   void clearSecurityContextAfterTest() {
    SecurityContextHolder.clearContext();
   }

    @Test
    void shouldSetUserIdInSecurityContextWhenJwtIsValid() throws Exception {

        // Arrange
        String token = "valid-token";

        when(jwtService.isTokenValid(token))
                .thenReturn(true);

        when(jwtService.extractUsername(token))
                .thenReturn("integrationuser");

        when(jwtService.extractUserId(token))
                .thenReturn(2L);

        when(jwtService.extractRole(token))
                .thenReturn("USER");

        MockHttpServletRequest request =
                new MockHttpServletRequest();

        request.addHeader(
                "Authorization",
                "Bearer " + token
        );

        MockHttpServletResponse response =
                new MockHttpServletResponse();

        FilterChain filterChain = mock(FilterChain.class);

        // Act
        filter.doFilter(request, response, filterChain);

        // Assert
        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        assertNotNull(authentication);

        assertEquals(
                "integrationuser",
                authentication.getPrincipal()
        );

        assertEquals(
                2L,
                authentication.getDetails()
        );

        assertTrue(
                authentication.getAuthorities()
                        .stream()
                        .anyMatch(authority ->
                                authority.getAuthority()
                                        .equals("ROLE_USER")
                        )
        );

        verify(filterChain)
                .doFilter(request, response);
    }

    @Test
    void shouldNotAuthenticateWhenAuthorizationHeaderIsMissing()
            throws Exception {

        // Arrange
        MockHttpServletRequest request =
                new MockHttpServletRequest();

        MockHttpServletResponse response =
                new MockHttpServletResponse();

        FilterChain filterChain = mock(FilterChain.class);

        // Act
        filter.doFilter(request, response, filterChain);

        // Assert
        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        assertNull(authentication);

        verify(filterChain)
                .doFilter(request, response);

        verifyNoInteractions(jwtService);
    }

    @Test
    void shouldNotAuthenticateWhenJwtIsInvalid()
            throws Exception {

        // Arrange
        String token = "invalid-token";

        when(jwtService.isTokenValid(token))
                .thenReturn(false);

        MockHttpServletRequest request =
                new MockHttpServletRequest();

        request.addHeader(
                "Authorization",
                "Bearer " + token
        );

        MockHttpServletResponse response =
                new MockHttpServletResponse();

        FilterChain filterChain = mock(FilterChain.class);

        // Act
        filter.doFilter(request, response, filterChain);

        // Assert
        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        assertNull(authentication);

        verify(filterChain)
                .doFilter(request, response);

        verify(jwtService)
                .isTokenValid(token);

        verify(jwtService, never())
                .extractUsername(token);

        verify(jwtService, never())
                .extractUserId(token);

        verify(jwtService, never())
                .extractRole(token);
    }
}