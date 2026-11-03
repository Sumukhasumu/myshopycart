package com.myshopycart.cartservice.dto;

import java.util.List;

public class WishlistResponse {

    private Long wishlistId;
    private Long userId;
    private List<WishlistItemResponse> items;

    public WishlistResponse() {
    }

    public WishlistResponse(
            Long wishlistId,
            Long userId,
            List<WishlistItemResponse> items) {

        this.wishlistId = wishlistId;
        this.userId = userId;
        this.items = items;
    }

    public Long getWishlistId() {
        return wishlistId;
    }

    public void setWishlistId(Long wishlistId) {
        this.wishlistId = wishlistId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public List<WishlistItemResponse> getItems() {
        return items;
    }

    public void setItems(List<WishlistItemResponse> items) {
        this.items = items;
    }
}