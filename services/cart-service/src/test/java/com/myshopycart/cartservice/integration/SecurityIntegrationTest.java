
package com.myshopycart.cartservice.integration;

import com.myshopycart.cartservice.entity.Cart;
import com.myshopycart.cartservice.repository.CartRepository;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class SecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CartRepository cartRepository;

    @Value("${myshopycart.auth.jwt-secret}")
    private String jwtSecret;

    @BeforeEach
    void cleanDatabase() {
        cartRepository.deleteAll();
    }

    @Test
    void getCart_withoutAuthentication_shouldReturn401()
            throws Exception {

        mockMvc.perform(get("/api/carts"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getWishlist_withoutAuthentication_shouldReturn401()
            throws Exception {

        mockMvc.perform(get("/api/wishlists"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getCart_withInvalidBearerToken_shouldReturn401()
            throws Exception {

        mockMvc.perform(
                get("/api/carts")
                        .header(
                                "Authorization",
                                "Bearer invalid.jwt.token"
                        )
        ).andExpect(status().isUnauthorized());
    }

    @Test
    void getWishlist_withInvalidBearerToken_shouldReturn401()
            throws Exception {

        mockMvc.perform(
                get("/api/wishlists")
                        .header(
                                "Authorization",
                                "Bearer invalid.jwt.token"
                        )
        ).andExpect(status().isUnauthorized());
    }

    
    @Test
    void getCart_withExpiredSignedJwt_shouldReturn401()
            throws Exception {

        String token = createExpiredToken(987654L);

        mockMvc.perform(
            	get("/api/carts")
                    	.header(
                            "Authorization",
                            "Bearer " + token
                   	)
    ).andExpect(status().isUnauthorized());
    }

    @Test
    void getCart_withValidSignedJwt_shouldReturnAuthenticatedUsersCart()
            throws Exception {

        Long userId = 987654L;

        Cart cart = new Cart();
        cart.setUserId(userId);
        cart.setCreatedAt(Instant.now());
        cart.setUpdatedAt(Instant.now());

        cartRepository.saveAndFlush(cart);

        String token = createToken(userId);

        mockMvc.perform(
                get("/api/carts")
                        .header(
                                "Authorization",
                                "Bearer " + token
                        )
                        .accept(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.userId").value(userId))
        .andExpect(jsonPath("$.items").isArray());
    }

    
private String createExpiredToken(Long userId) {

    SecretKey signingKey = Keys.hmacShaKeyFor(
            jwtSecret.getBytes(StandardCharsets.UTF_8)
    );

    Instant now = Instant.now();

    return Jwts.builder()
            .subject("security-test-user")
            .claim("userId", userId)
            .claim("role", "CUSTOMER")
            .issuedAt(Date.from(now.minusSeconds(600)))
            .expiration(Date.from(now.minusSeconds(300)))
            .signWith(signingKey)
            .compact();
}

    private String createToken(Long userId) {

        SecretKey signingKey = Keys.hmacShaKeyFor(
                jwtSecret.getBytes(StandardCharsets.UTF_8)
        );

        Instant now = Instant.now();

        return Jwts.builder()
                .subject("security-test-user")
                .claim("userId", userId)
                .claim("role", "CUSTOMER")
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(300)))
                .signWith(signingKey)
                .compact();
    }
}