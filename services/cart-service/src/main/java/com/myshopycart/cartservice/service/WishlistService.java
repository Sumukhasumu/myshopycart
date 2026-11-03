package com.myshopycart.cartservice.service;

import com.myshopycart.cartservice.dto.WishlistItemResponse;
import com.myshopycart.cartservice.dto.WishlistResponse;
import com.myshopycart.cartservice.entity.Wishlist;
import com.myshopycart.cartservice.entity.WishlistItem;
import com.myshopycart.cartservice.exception.ResourceNotFoundException;
import com.myshopycart.cartservice.repository.WishlistItemRepository;
import com.myshopycart.cartservice.repository.WishlistRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
public class WishlistService {

    private final WishlistRepository wishlistRepository;
    private final WishlistItemRepository wishlistItemRepository;

    public WishlistService(
            WishlistRepository wishlistRepository,
            WishlistItemRepository wishlistItemRepository) {

        this.wishlistRepository = wishlistRepository;
        this.wishlistItemRepository = wishlistItemRepository;
    }

    @Transactional
    public WishlistItem addProductToWishlist(
            Long userId,
            String productId) {

        Wishlist wishlist = wishlistRepository
                .findByUserId(userId)
                .orElseGet(() -> createWishlist(userId));

        WishlistItem existingItem =
                wishlistItemRepository
                        .findByWishlistAndProductId(
                                wishlist,
                                productId)
                        .orElse(null);

        if (existingItem != null) {
            return existingItem;
        }

        Instant now = Instant.now();

        WishlistItem wishlistItem = new WishlistItem();

        wishlistItem.setWishlist(wishlist);
        wishlistItem.setProductId(productId);
        wishlistItem.setCreatedAt(now);

        wishlist.setUpdatedAt(now);

        wishlistRepository.save(wishlist);

        return wishlistItemRepository.save(wishlistItem);
    }

    @Transactional
    public void removeProductFromWishlist(
            Long userId,
            String productId) {

        Wishlist wishlist = wishlistRepository
                .findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Wishlist not found for user: " + userId));

        WishlistItem wishlistItem =
                wishlistItemRepository
                        .findByWishlistAndProductId(
                                wishlist,
                                productId)
                        .orElseThrow(() -> new ResourceNotFoundException(
                                "Product not found in wishlist: "
                                        + productId));

        wishlistItemRepository.delete(wishlistItem);

        wishlist.setUpdatedAt(Instant.now());

        wishlistRepository.save(wishlist);
    }

    @Transactional(readOnly = true)
    public WishlistResponse getWishlist(Long userId) {

        Wishlist wishlist = wishlistRepository
                .findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Wishlist not found for user: " + userId));

        List<WishlistItemResponse> items =
                wishlistItemRepository
                        .findByWishlist(wishlist)
                        .stream()
                        .map(this::toWishlistItemResponse)
                        .toList();

        return new WishlistResponse(
                wishlist.getId(),
                wishlist.getUserId(),
                items);
    }

    private WishlistItemResponse toWishlistItemResponse(
            WishlistItem wishlistItem) {

        return new WishlistItemResponse(
                wishlistItem.getId(),
                wishlistItem.getWishlist().getId(),
                wishlistItem.getProductId());
    }

    private Wishlist createWishlist(Long userId) {

        Instant now = Instant.now();

        Wishlist wishlist = new Wishlist();

        wishlist.setUserId(userId);
        wishlist.setCreatedAt(now);
        wishlist.setUpdatedAt(now);

        return wishlistRepository.save(wishlist);
    }
}