package com.myshopycart.cartservice.dto;

public class WishlistItemResponse {

    private Long wishlistItemId;
    private Long wishlistId;
    private String productId;

    public WishlistItemResponse() {
    }

    public WishlistItemResponse(
            Long wishlistItemId,
            Long wishlistId,
            String productId) {

        this.wishlistItemId = wishlistItemId;
        this.wishlistId = wishlistId;
        this.productId = productId;
    }

    public Long getWishlistItemId() {
        return wishlistItemId;
    }

    public void setWishlistItemId(Long wishlistItemId) {
        this.wishlistItemId = wishlistItemId;
    }

    public Long getWishlistId() {
        return wishlistId;
    }

    public void setWishlistId(Long wishlistId) {
        this.wishlistId = wishlistId;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }
}