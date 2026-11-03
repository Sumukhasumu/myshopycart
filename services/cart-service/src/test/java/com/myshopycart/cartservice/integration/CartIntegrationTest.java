package com.myshopycart.cartservice.integration;

import com.myshopycart.cartservice.client.ProductClient;
import com.myshopycart.cartservice.entity.Cart;
import com.myshopycart.cartservice.entity.CartItem;
import com.myshopycart.cartservice.repository.CartItemRepository;
import com.myshopycart.cartservice.repository.CartRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class CartIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    @MockBean
    private ProductClient productClient;

    @BeforeEach
    void cleanDatabase() {

        cartItemRepository.deleteAll();
        cartRepository.deleteAll();
    }

    @Test
    void addProductToCart_shouldCreateCartAndItem() throws Exception {

        when(productClient.productExists("product-test-1"))
                .thenReturn(true);

        UsernamePasswordAuthenticationToken authentication =
                createAuthentication(4L);

        mockMvc.perform(
                post("/api/carts/items")
                        .with(authentication(authentication))
                        .contentType("application/json")
                        .content("""
                                {
                                    "productId": "product-test-1",
                                    "quantity": 2
                                }
                                """)
        )
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.productId")
                .value("product-test-1"))
        .andExpect(jsonPath("$.quantity")
                .value(2));

        Cart cart = cartRepository
                .findByUserId(4L)
                .orElseThrow();

        assertEquals(4L, cart.getUserId());

        List<CartItem> cartItems =
                cartItemRepository.findByCart(cart);

        assertEquals(1, cartItems.size());
        assertEquals(
                "product-test-1",
                cartItems.get(0).getProductId());
        assertEquals(
                2,
                cartItems.get(0).getQuantity());
    }

    @Test
    void addProductToCart_whenItemAlreadyExists_shouldIncreaseQuantity()
            throws Exception {

        when(productClient.productExists("product-test-1"))
                .thenReturn(true);

        UsernamePasswordAuthenticationToken authentication =
                createAuthentication(4L);

        mockMvc.perform(
                post("/api/carts/items")
                        .with(authentication(authentication))
                        .contentType("application/json")
                        .content("""
                                {
                                    "productId": "product-test-1",
                                    "quantity": 2
                                }
                                """)
        )
        .andExpect(status().isCreated());

        mockMvc.perform(
                post("/api/carts/items")
                        .with(authentication(authentication))
                        .contentType("application/json")
                        .content("""
                                {
                                    "productId": "product-test-1",
                                    "quantity": 3
                                }
                                """)
        )
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.quantity")
                .value(5));

        Cart cart = cartRepository
                .findByUserId(4L)
                .orElseThrow();

        List<CartItem> cartItems =
                cartItemRepository.findByCart(cart);

        assertEquals(1, cartItems.size());
        assertEquals(5, cartItems.get(0).getQuantity());
    }

    @Test
    void updateCartQuantity_shouldUpdateExistingItem()
            throws Exception {

        when(productClient.productExists("product-test-1"))
                .thenReturn(true);

        UsernamePasswordAuthenticationToken authentication =
                createAuthentication(4L);

        mockMvc.perform(
                post("/api/carts/items")
                        .with(authentication(authentication))
                        .contentType("application/json")
                        .content("""
                                {
                                    "productId": "product-test-1",
                                    "quantity": 2
                                }
                                """)
        )
        .andExpect(status().isCreated());

        mockMvc.perform(
                put("/api/carts/items/product-test-1")
                        .with(authentication(authentication))
                        .contentType("application/json")
                        .content("""
                                {
                                    "quantity": 10
                                }
                                """)
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.productId")
                .value("product-test-1"))
        .andExpect(jsonPath("$.quantity")
                .value(10));

        Cart cart = cartRepository
                .findByUserId(4L)
                .orElseThrow();

        List<CartItem> cartItems =
                cartItemRepository.findByCart(cart);

        assertEquals(1, cartItems.size());
        assertEquals(
                "product-test-1",
                cartItems.get(0).getProductId());
        assertEquals(
                10,
                cartItems.get(0).getQuantity());
    }

    @Test
    void removeProductFromCart_shouldDeleteCartItem()
            throws Exception {

        when(productClient.productExists("product-test-1"))
                .thenReturn(true);

        UsernamePasswordAuthenticationToken authentication =
                createAuthentication(4L);

        // First add the product to the cart.
        mockMvc.perform(
                post("/api/carts/items")
                        .with(authentication(authentication))
                        .contentType("application/json")
                        .content("""
                                {
                                    "productId": "product-test-1",
                                    "quantity": 2
                                }
                                """)
        )
        .andExpect(status().isCreated());

        // Verify the item exists before deletion.
        Cart cart = cartRepository
                .findByUserId(4L)
                .orElseThrow();

        List<CartItem> itemsBeforeDelete =
                cartItemRepository.findByCart(cart);

        assertEquals(1, itemsBeforeDelete.size());

        // Remove the product.
        mockMvc.perform(
                delete("/api/carts/items/product-test-1")
                        .with(authentication(authentication))
        )
        .andExpect(status().isNoContent());

        // Verify the item was deleted from PostgreSQL.
        List<CartItem> itemsAfterDelete =
                cartItemRepository.findByCart(cart);

        assertEquals(0, itemsAfterDelete.size());
    }

    private UsernamePasswordAuthenticationToken createAuthentication(
            Long userId) {

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        "admin",
                        null,
                        List.of(
                                new SimpleGrantedAuthority("ROLE_ADMIN")
                        )
                );

        authentication.setDetails(userId);

        return authentication;
    }
}