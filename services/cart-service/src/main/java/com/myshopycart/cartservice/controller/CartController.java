package com.myshopycart.cartservice.controller;

import com.myshopycart.cartservice.dto.AddToCartRequest;
import com.myshopycart.cartservice.dto.CartItemResponse;
import com.myshopycart.cartservice.dto.CartResponse;
import com.myshopycart.cartservice.dto.UpdateCartQuantityRequest;
import com.myshopycart.cartservice.entity.CartItem;
import com.myshopycart.cartservice.service.CartService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/carts")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @PostMapping("/items")
    public ResponseEntity<CartItemResponse> addProductToCart(
            Authentication authentication,
            @Valid @RequestBody AddToCartRequest request) {

        Long userId = getAuthenticatedUserId(authentication);

        CartItem cartItem = cartService.addProductToCart(
                userId,
                request.getProductId(),
                request.getQuantity());

        CartItemResponse response = toResponse(cartItem);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @PutMapping("/items/{productId}")
    public ResponseEntity<CartItemResponse> updateCartQuantity(
            Authentication authentication,
            @PathVariable String productId,
            @Valid @RequestBody UpdateCartQuantityRequest request) {

        Long userId = getAuthenticatedUserId(authentication);

        CartItem cartItem = cartService.updateCartQuantity(
                userId,
                productId,
                request.getQuantity());

        CartItemResponse response = toResponse(cartItem);

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/items/{productId}")
    public ResponseEntity<Void> removeProductFromCart(
            Authentication authentication,
            @PathVariable String productId) {

        Long userId = getAuthenticatedUserId(authentication);

        cartService.removeProductFromCart(
                userId,
                productId);

        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<CartResponse> getCart(
            Authentication authentication) {

        Long userId = getAuthenticatedUserId(authentication);

        CartResponse response = cartService.getCart(userId);

        return ResponseEntity.ok(response);
    }

    @DeleteMapping
    public ResponseEntity<Void> clearCart(
            Authentication authentication) {

        Long userId = getAuthenticatedUserId(authentication);

        cartService.clearCart(userId);

        return ResponseEntity.noContent().build();
    }

    private Long getAuthenticatedUserId(
            Authentication authentication) {

        if (authentication == null ||
                authentication.getDetails() == null) {

            throw new IllegalStateException(
                    "Authenticated user ID not available");
        }

        return (Long) authentication.getDetails();
    }

    private CartItemResponse toResponse(CartItem cartItem) {

        return new CartItemResponse(
                cartItem.getId(),
                cartItem.getCart().getId(),
                cartItem.getProductId(),
                cartItem.getQuantity());
    }
}