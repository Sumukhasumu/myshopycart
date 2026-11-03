package com.myshopycart.cartservice.controller;

import com.myshopycart.cartservice.entity.Wishlist;
import com.myshopycart.cartservice.entity.WishlistItem;
import com.myshopycart.cartservice.service.WishlistService;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

import org.springframework.http.HttpStatus;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;

import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(WishlistController.class)
@Import(WishlistControllerTest.TestSecurityConfig.class)
class WishlistControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private WishlistService wishlistService;

    @Test
    void addProductToWishlist_withoutAuthentication_shouldReturn401()
            throws Exception {

        mockMvc.perform(
                post("/api/wishlists/items")
                        .contentType("application/json")
                        .content("""
                                {
                                  "productId": "product-test-1"
                                }
                                """))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void addProductToWishlist_withMissingProductId_shouldReturn400()
            throws Exception {

        Authentication authentication =
                createAuthentication(4L);

        mockMvc.perform(
                post("/api/wishlists/items")
                        .with(authentication(authentication))
                        .contentType("application/json")
                        .content("""
                                {
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addProductToWishlist_withValidRequest_shouldReturn201()
            throws Exception {

        Authentication authentication =
                createAuthentication(4L);

        Wishlist wishlist = new Wishlist();
        wishlist.setId(2L);
        wishlist.setUserId(4L);

        WishlistItem savedWishlistItem =
                new WishlistItem();

        savedWishlistItem.setId(1L);
        savedWishlistItem.setWishlist(wishlist);
        savedWishlistItem.setProductId("product-test-1");

        when(wishlistService.addProductToWishlist(
                eq(4L),
                eq("product-test-1")))
                .thenReturn(savedWishlistItem);

        mockMvc.perform(
                post("/api/wishlists/items")
                        .with(authentication(authentication))
                        .contentType("application/json")
                        .content("""
                                {
                                  "productId": "product-test-1"
                                }
                                """))
                .andExpect(status().isCreated());

        verify(wishlistService)
                .addProductToWishlist(
                        eq(4L),
                        eq("product-test-1"));
    }

    private Authentication createAuthentication(
            Long userId) {

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        "admin",
                        null,
                        List.of(
                                new SimpleGrantedAuthority(
                                        "ROLE_ADMIN")
                        )
                );

        authentication.setDetails(userId);

        return authentication;
    }

    @TestConfiguration
    static class TestSecurityConfig {

        @Bean
        SecurityFilterChain testSecurityFilterChain(
                HttpSecurity http) throws Exception {

            http
                    .csrf(csrf -> csrf.disable())

                    .exceptionHandling(exception -> exception
                            .authenticationEntryPoint(
                                    authenticationEntryPoint())
                    )

                    .authorizeHttpRequests(auth -> auth
                            .anyRequest()
                            .authenticated());

            return http.build();
        }

        @Bean
        AuthenticationEntryPoint authenticationEntryPoint() {

            return (request, response, authException) ->
                    response.sendError(
                            HttpStatus.UNAUTHORIZED.value(),
                            "Authentication is required");
        }
    }
}