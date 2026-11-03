package com.myshopycart.cartservice.repository;

import com.myshopycart.cartservice.entity.Wishlist;
import com.myshopycart.cartservice.entity.WishlistItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface WishlistItemRepository
        extends JpaRepository<WishlistItem, Long> {

    Optional<WishlistItem> findByWishlistAndProductId(
            Wishlist wishlist,
            String productId);

    List<WishlistItem> findByWishlist(
            Wishlist wishlist);

    void deleteByWishlistAndProductId(
            Wishlist wishlist,
            String productId);
}