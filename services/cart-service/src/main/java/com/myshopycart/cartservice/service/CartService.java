package com.myshopycart.cartservice.service;

import com.myshopycart.cartservice.client.ProductClient;
import com.myshopycart.cartservice.dto.CartItemResponse;
import com.myshopycart.cartservice.dto.CartResponse;
import com.myshopycart.cartservice.entity.Cart;
import com.myshopycart.cartservice.entity.CartItem;
import com.myshopycart.cartservice.exception.ResourceNotFoundException;
import com.myshopycart.cartservice.repository.CartItemRepository;
import com.myshopycart.cartservice.repository.CartRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductClient productClient;

    public CartService(
            CartRepository cartRepository,
            CartItemRepository cartItemRepository,
            ProductClient productClient) {

        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.productClient = productClient;
    }

    @Transactional
    public CartItem addProductToCart(
            Long userId,
            String productId,
            Integer quantity) {

        if (!productClient.productExists(productId)) {
            throw new ResourceNotFoundException(
                    "Product not found: " + productId);
        }

        Cart cart = cartRepository.findByUserId(userId)
                .orElseGet(() -> createCart(userId));

        CartItem cartItem = cartItemRepository
                .findByCartAndProductId(cart, productId)
                .orElse(null);

        Instant now = Instant.now();

        if (cartItem != null) {
            cartItem.setQuantity(
                    cartItem.getQuantity() + quantity);
            cartItem.setUpdatedAt(now);
        } else {
            cartItem = new CartItem();
            cartItem.setCart(cart);
            cartItem.setProductId(productId);
            cartItem.setQuantity(quantity);
            cartItem.setCreatedAt(now);
            cartItem.setUpdatedAt(now);
        }

        cart.setUpdatedAt(now);
        cartRepository.save(cart);

        return cartItemRepository.save(cartItem);
    }

    @Transactional
    public CartItem updateCartQuantity(
            Long userId,
            String productId,
            Integer quantity) {

        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Cart not found for user: " + userId));

        CartItem cartItem = cartItemRepository
                .findByCartAndProductId(cart, productId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Product not found in cart: " + productId));

        cartItem.setQuantity(quantity);
        cartItem.setUpdatedAt(Instant.now());
        cart.setUpdatedAt(Instant.now());

        cartRepository.save(cart);

        return cartItemRepository.save(cartItem);
    }

    @Transactional
    public void removeProductFromCart(
            Long userId,
            String productId) {

        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Cart not found for user: " + userId));

        CartItem cartItem = cartItemRepository
                .findByCartAndProductId(cart, productId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Product not found in cart: " + productId));

        cartItemRepository.delete(cartItem);

        cart.setUpdatedAt(Instant.now());
        cartRepository.save(cart);
    }

    @Transactional(readOnly = true)
    public CartResponse getCart(Long userId) {

        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Cart not found for user: " + userId));

        List<CartItemResponse> items = cartItemRepository
                .findByCart(cart)
                .stream()
                .map(this::toCartItemResponse)
                .toList();

        return new CartResponse(
                cart.getId(),
                cart.getUserId(),
                items);
    }

    @Transactional
    public void clearCart(Long userId) {

        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Cart not found for user: " + userId));

        List<CartItem> cartItems =
                cartItemRepository.findByCart(cart);

        cartItemRepository.deleteAll(cartItems);

        cart.setUpdatedAt(Instant.now());
        cartRepository.save(cart);
    }

    private CartItemResponse toCartItemResponse(
            CartItem cartItem) {

        return new CartItemResponse(
                cartItem.getId(),
                cartItem.getCart().getId(),
                cartItem.getProductId(),
                cartItem.getQuantity());
    }

    private Cart createCart(Long userId) {

        Instant now = Instant.now();

        Cart cart = new Cart();
        cart.setUserId(userId);
        cart.setCreatedAt(now);
        cart.setUpdatedAt(now);

        return cartRepository.save(cart);
    }
}