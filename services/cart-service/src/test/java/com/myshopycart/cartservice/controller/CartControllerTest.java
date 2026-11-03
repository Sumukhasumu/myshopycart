package com.myshopycart.cartservice.controller;

import com.myshopycart.cartservice.entity.CartItem;
import com.myshopycart.cartservice.service.CartService;

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
import com.myshopycart.cartservice.entity.Cart;

import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CartController.class)
@Import(CartControllerTest.TestSecurityConfig.class)
class CartControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CartService cartService;

    @Test
    void addProductToCart_withoutAuthentication_shouldReturn401()
            throws Exception {

        mockMvc.perform(
                post("/api/carts/items")
                        .contentType("application/json")
                        .content("""
                                {
                                  "productId": "product-test-1",
                                  "quantity": 2
                                }
                                """))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void addProductToCart_withInvalidQuantity_shouldReturn400()
            throws Exception {

        Authentication authentication =
                createAuthentication(4L);

        mockMvc.perform(
                post("/api/carts/items")
                        .with(authentication(authentication))
                        .contentType("application/json")
                        .content("""
                                {
                                  "productId": "product-test-1",
                                  "quantity": 0
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addProductToCart_withMissingProductId_shouldReturn400()
            throws Exception {

        Authentication authentication =
                createAuthentication(4L);

        mockMvc.perform(
                post("/api/carts/items")
                        .with(authentication(authentication))
                        .contentType("application/json")
                        .content("""
                                {
                                  "quantity": 2
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addProductToCart_withValidRequest_shouldReturn201()
            throws Exception {

        Authentication authentication =
                createAuthentication(4L);

        Cart cart = new Cart();

	cart.setId(2L);
	cart.setUserId(4L);

	CartItem savedCartItem = new CartItem();

	savedCartItem.setId(1L);
	savedCartItem.setCart(cart);
	savedCartItem.setProductId("product-test-1");
	savedCartItem.setQuantity(2);

        when(cartService.addProductToCart(
                eq(4L),
                eq("product-test-1"),
                eq(2)))
                .thenReturn(savedCartItem);

        mockMvc.perform(
                post("/api/carts/items")
                        .with(authentication(authentication))
                        .contentType("application/json")
                        .content("""
                                {
                                  "productId": "product-test-1",
                                  "quantity": 2
                                }
                                """))
                .andExpect(status().isCreated());

        verify(cartService).addProductToCart(
                eq(4L),
                eq("product-test-1"),
                eq(2));
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
                            .authenticated()
                    );

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