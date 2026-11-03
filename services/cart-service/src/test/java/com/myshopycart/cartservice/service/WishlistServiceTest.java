package com.myshopycart.cartservice.service;

import com.myshopycart.cartservice.entity.Wishlist;
import com.myshopycart.cartservice.entity.WishlistItem;
import com.myshopycart.cartservice.exception.ResourceNotFoundException;
import com.myshopycart.cartservice.repository.WishlistItemRepository;
import com.myshopycart.cartservice.repository.WishlistRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WishlistServiceTest {

    @Mock
    private WishlistRepository wishlistRepository;

    @Mock
    private WishlistItemRepository wishlistItemRepository;

    @InjectMocks
    private WishlistService wishlistService;

    @Test
    void addProductToWishlist_shouldCreateNewItem() {

        Long userId = 4L;
        String productId = "product-test-1";

        when(wishlistRepository.findByUserId(userId))
                .thenReturn(Optional.empty());

        Wishlist savedWishlist = new Wishlist();
        savedWishlist.setId(1L);
        savedWishlist.setUserId(userId);

        when(wishlistRepository.save(any(Wishlist.class)))
                .thenReturn(savedWishlist);

        when(wishlistItemRepository
                .findByWishlistAndProductId(
                        savedWishlist,
                        productId))
                .thenReturn(Optional.empty());

        WishlistItem savedItem = new WishlistItem();
        savedItem.setId(1L);
        savedItem.setWishlist(savedWishlist);
        savedItem.setProductId(productId);

        when(wishlistItemRepository.save(any(WishlistItem.class)))
                .thenReturn(savedItem);

        WishlistItem result =
                wishlistService.addProductToWishlist(
                        userId,
                        productId);

        assertNotNull(result);
        assertEquals(productId, result.getProductId());
        assertEquals(
                userId,
                result.getWishlist().getUserId());

        verify(wishlistRepository)
                .findByUserId(userId);

        verify(wishlistRepository, times(2))
                .save(any(Wishlist.class));

        verify(wishlistItemRepository)
                .findByWishlistAndProductId(
                        savedWishlist,
                        productId);

        verify(wishlistItemRepository)
                .save(any(WishlistItem.class));
    }

    @Test
    void addProductToWishlist_shouldReturnExistingItem() {

        Long userId = 4L;
        String productId = "product-test-1";

        Wishlist wishlist = new Wishlist();
        wishlist.setId(1L);
        wishlist.setUserId(userId);

        WishlistItem existingItem = new WishlistItem();
        existingItem.setId(1L);
        existingItem.setWishlist(wishlist);
        existingItem.setProductId(productId);

        when(wishlistRepository.findByUserId(userId))
                .thenReturn(Optional.of(wishlist));

        when(wishlistItemRepository
                .findByWishlistAndProductId(
                        wishlist,
                        productId))
                .thenReturn(Optional.of(existingItem));

        WishlistItem result =
                wishlistService.addProductToWishlist(
                        userId,
                        productId);

        assertSame(existingItem, result);

        verify(wishlistRepository)
                .findByUserId(userId);

        verify(wishlistItemRepository)
                .findByWishlistAndProductId(
                        wishlist,
                        productId);

        verify(wishlistItemRepository, never())
                .save(any(WishlistItem.class));
    }

    @Test
    void removeProductFromWishlist_shouldThrowWhenWishlistDoesNotExist() {

        Long userId = 4L;
        String productId = "product-test-1";

        when(wishlistRepository.findByUserId(userId))
                .thenReturn(Optional.empty());

        ResourceNotFoundException exception =
                assertThrows(
                        ResourceNotFoundException.class,
                        () -> wishlistService
                                .removeProductFromWishlist(
                                        userId,
                                        productId));

        assertEquals(
                "Wishlist not found for user: 4",
                exception.getMessage());

        verify(wishlistRepository)
                .findByUserId(userId);

        verify(wishlistItemRepository, never())
                .delete(any(WishlistItem.class));
    }

    @Test
    void removeProductFromWishlist_shouldThrowWhenItemDoesNotExist() {

        Long userId = 4L;
        String productId = "PRODUCT-NOT-IN-WISHLIST";

        Wishlist wishlist = new Wishlist();
        wishlist.setId(1L);
        wishlist.setUserId(userId);

        when(wishlistRepository.findByUserId(userId))
                .thenReturn(Optional.of(wishlist));

        when(wishlistItemRepository
                .findByWishlistAndProductId(
                        wishlist,
                        productId))
                .thenReturn(Optional.empty());

        ResourceNotFoundException exception =
                assertThrows(
                        ResourceNotFoundException.class,
                        () -> wishlistService
                                .removeProductFromWishlist(
                                        userId,
                                        productId));

        assertEquals(
                "Product not found in wishlist: "
                        + productId,
                exception.getMessage());

        verify(wishlistRepository)
                .findByUserId(userId);

        verify(wishlistItemRepository)
                .findByWishlistAndProductId(
                        wishlist,
                        productId);

        verify(wishlistItemRepository, never())
                .delete(any(WishlistItem.class));
    }
}