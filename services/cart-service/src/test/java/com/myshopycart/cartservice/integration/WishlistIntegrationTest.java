package com.myshopycart.cartservice.integration;

import com.myshopycart.cartservice.entity.Wishlist;
import com.myshopycart.cartservice.entity.WishlistItem;
import com.myshopycart.cartservice.repository.WishlistItemRepository;
import com.myshopycart.cartservice.repository.WishlistRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class WishlistIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private WishlistRepository wishlistRepository;

    @Autowired
    private WishlistItemRepository wishlistItemRepository;

    @BeforeEach
    void cleanDatabase() {

        wishlistItemRepository.deleteAll();
        wishlistRepository.deleteAll();
    }

    @Test
    void addProductToWishlist_shouldCreateWishlistAndItem()
            throws Exception {

        UsernamePasswordAuthenticationToken authentication =
                createAuthentication(4L);

        mockMvc.perform(
                post("/api/wishlists/items")
                        .with(authentication(authentication))
                        .contentType("application/json")
                        .content("""
                                {
                                    "productId": "product-test-1"
                                }
                                """)
        )
        .andExpect(status().isCreated());

        Wishlist wishlist = wishlistRepository
                .findByUserId(4L)
                .orElseThrow();

        assertEquals(
                4L,
                wishlist.getUserId()
        );

        List<WishlistItem> wishlistItems =
                wishlistItemRepository
                        .findByWishlist(wishlist);

        assertEquals(
                1,
                wishlistItems.size()
        );

        assertEquals(
                "product-test-1",
                wishlistItems.get(0).getProductId()
        );

        assertEquals(
                wishlist.getId(),
                wishlistItems.get(0)
                        .getWishlist()
                        .getId()
        );
    }

    @Test
    void addProductToWishlist_whenItemAlreadyExists_shouldNotCreateDuplicate()
            throws Exception {

        UsernamePasswordAuthenticationToken authentication =
                createAuthentication(4L);

        mockMvc.perform(
                post("/api/wishlists/items")
                        .with(authentication(authentication))
                        .contentType("application/json")
                        .content("""
                                {
                                    "productId": "product-test-1"
                                }
                                """)
        )
        .andExpect(status().isCreated());

        Wishlist wishlist = wishlistRepository
                .findByUserId(4L)
                .orElseThrow();

        List<WishlistItem> itemsAfterFirstRequest =
                wishlistItemRepository
                        .findByWishlist(wishlist);

        assertEquals(
                1,
                itemsAfterFirstRequest.size()
        );

        Long firstItemId =
                itemsAfterFirstRequest.get(0).getId();

        mockMvc.perform(
                post("/api/wishlists/items")
                        .with(authentication(authentication))
                        .contentType("application/json")
                        .content("""
                                {
                                    "productId": "product-test-1"
                                }
                                """)
        )
        .andExpect(status().isCreated());

        List<WishlistItem> itemsAfterSecondRequest =
                wishlistItemRepository
                        .findByWishlist(wishlist);

        assertEquals(
                1,
                itemsAfterSecondRequest.size()
        );

        assertEquals(
                firstItemId,
                itemsAfterSecondRequest.get(0).getId()
        );
    }

    @Test
    void removeProductFromWishlist_shouldDeleteWishlistItem()
            throws Exception {

        UsernamePasswordAuthenticationToken authentication =
                createAuthentication(4L);

        // Add the product first.
        mockMvc.perform(
                post("/api/wishlists/items")
                        .with(authentication(authentication))
                        .contentType("application/json")
                        .content("""
                                {
                                    "productId": "product-test-1"
                                }
                                """)
        )
        .andExpect(status().isCreated());

        Wishlist wishlist = wishlistRepository
                .findByUserId(4L)
                .orElseThrow();

        List<WishlistItem> itemsBeforeDelete =
                wishlistItemRepository
                        .findByWishlist(wishlist);

        assertEquals(
                1,
                itemsBeforeDelete.size()
        );

        // Remove the product.
        mockMvc.perform(
                delete("/api/wishlists/items/product-test-1")
                        .with(authentication(authentication))
        )
        .andExpect(status().isNoContent());

        // Verify deletion in PostgreSQL.
        List<WishlistItem> itemsAfterDelete =
                wishlistItemRepository
                        .findByWishlist(wishlist);

        assertEquals(
                0,
                itemsAfterDelete.size()
        );
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