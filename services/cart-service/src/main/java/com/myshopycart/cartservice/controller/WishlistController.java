package com.myshopycart.cartservice.controller;

import com.myshopycart.cartservice.dto.AddToWishlistRequest;
import com.myshopycart.cartservice.dto.WishlistItemResponse;
import com.myshopycart.cartservice.dto.WishlistResponse;
import com.myshopycart.cartservice.entity.WishlistItem;
import com.myshopycart.cartservice.service.WishlistService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/wishlists")
public class WishlistController {

    private final WishlistService wishlistService;

    public WishlistController(WishlistService wishlistService) {
        this.wishlistService = wishlistService;
    }

    @PostMapping("/items")
    public ResponseEntity<WishlistItemResponse> addProductToWishlist(
            Authentication authentication,
            @Valid @RequestBody AddToWishlistRequest request) {

        Long userId = getAuthenticatedUserId(authentication);

        WishlistItem wishlistItem =
                wishlistService.addProductToWishlist(
                        userId,
                        request.getProductId());

        WishlistItemResponse response =
                new WishlistItemResponse(
                        wishlistItem.getId(),
                        wishlistItem.getWishlist().getId(),
                        wishlistItem.getProductId());

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @DeleteMapping("/items/{productId}")
    public ResponseEntity<Void> removeProductFromWishlist(
            Authentication authentication,
            @PathVariable String productId) {

        Long userId = getAuthenticatedUserId(authentication);

        wishlistService.removeProductFromWishlist(
                userId,
                productId);

        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<WishlistResponse> getWishlist(
            Authentication authentication) {

        Long userId = getAuthenticatedUserId(authentication);

        WishlistResponse response =
                wishlistService.getWishlist(userId);

        return ResponseEntity.ok(response);
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
}