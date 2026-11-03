package com.myshopycart.cartservice.service;

import com.myshopycart.cartservice.client.ProductClient;
import com.myshopycart.cartservice.entity.Cart;
import com.myshopycart.cartservice.entity.CartItem;
import com.myshopycart.cartservice.exception.ResourceNotFoundException;
import com.myshopycart.cartservice.repository.CartItemRepository;
import com.myshopycart.cartservice.repository.CartRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private CartItemRepository cartItemRepository;

    @Mock
    private ProductClient productClient;

    @InjectMocks
    private CartService cartService;

    @Test
    void addProductToCart_shouldCreateNewCartItem() {

        Long userId = 4L;
        String productId = "product-test-1";
        Integer quantity = 2;

        when(productClient.productExists(productId))
                .thenReturn(true);

        when(cartRepository.findByUserId(userId))
                .thenReturn(Optional.empty());

        Cart savedCart = new Cart();
        savedCart.setId(1L);
        savedCart.setUserId(userId);

        when(cartRepository.save(any(Cart.class)))
                .thenReturn(savedCart);

        when(cartItemRepository
                .findByCartAndProductId(
                        any(Cart.class),
                        eq(productId)))
                .thenReturn(Optional.empty());

        CartItem savedCartItem = new CartItem();
        savedCartItem.setId(1L);
        savedCartItem.setCart(savedCart);
        savedCartItem.setProductId(productId);
        savedCartItem.setQuantity(quantity);

        when(cartItemRepository.save(any(CartItem.class)))
                .thenReturn(savedCartItem);

        CartItem result = cartService.addProductToCart(
                userId,
                productId,
                quantity);

        assertNotNull(result);
        assertEquals(productId, result.getProductId());
        assertEquals(quantity, result.getQuantity());
        assertEquals(userId, result.getCart().getUserId());

        verify(productClient)
                .productExists(productId);

        verify(cartRepository)
                .findByUserId(userId);

        verify(cartRepository, times(2))
                .save(any(Cart.class));

        verify(cartItemRepository)
                .findByCartAndProductId(
                        any(Cart.class),
                        eq(productId));

        verify(cartItemRepository)
                .save(any(CartItem.class));
    }

    @Test
    void addProductToCart_shouldIncreaseQuantityForExistingItem() {

        Long userId = 4L;
        String productId = "product-test-1";

        Cart cart = new Cart();
        cart.setId(1L);
        cart.setUserId(userId);

        CartItem existingItem = new CartItem();
        existingItem.setId(1L);
        existingItem.setCart(cart);
        existingItem.setProductId(productId);
        existingItem.setQuantity(2);

        when(productClient.productExists(productId))
                .thenReturn(true);

        when(cartRepository.findByUserId(userId))
                .thenReturn(Optional.of(cart));

        when(cartItemRepository
                .findByCartAndProductId(cart, productId))
                .thenReturn(Optional.of(existingItem));

        when(cartItemRepository.save(existingItem))
                .thenReturn(existingItem);

        CartItem result = cartService.addProductToCart(
                userId,
                productId,
                3);

        assertNotNull(result);
        assertEquals(5, result.getQuantity());

        verify(productClient)
                .productExists(productId);

        verify(cartRepository)
                .findByUserId(userId);

        verify(cartRepository)
                .save(cart);

        verify(cartItemRepository)
                .findByCartAndProductId(cart, productId);

        verify(cartItemRepository)
                .save(existingItem);
    }

    @Test
    void updateCartQuantity_shouldThrowWhenCartDoesNotExist() {

        Long userId = 4L;
        String productId = "product-test-1";

        when(cartRepository.findByUserId(userId))
                .thenReturn(Optional.empty());

        ResourceNotFoundException exception =
                assertThrows(
                        ResourceNotFoundException.class,
                        () -> cartService.updateCartQuantity(
                                userId,
                                productId,
                                5));

        assertEquals(
                "Cart not found for user: 4",
                exception.getMessage());

        verify(cartRepository)
                .findByUserId(userId);

        verify(cartItemRepository, never())
                .save(any(CartItem.class));
    }

    @Test
    void addProductToCart_shouldThrowWhenProductDoesNotExist() {

        Long userId = 4L;
        String productId = "PRODUCT-DOES-NOT-EXIST";

        when(productClient.productExists(productId))
                .thenReturn(false);

        ResourceNotFoundException exception =
                assertThrows(
                        ResourceNotFoundException.class,
                        () -> cartService.addProductToCart(
                                userId,
                                productId,
                                2));

        assertEquals(
                "Product not found: PRODUCT-DOES-NOT-EXIST",
                exception.getMessage());

        verify(productClient)
                .productExists(productId);

        verify(cartRepository, never())
                .save(any(Cart.class));

        verify(cartItemRepository, never())
                .save(any(CartItem.class));
    }
}