package com.myshopycart.cartservice.dto;

import jakarta.validation.constraints.NotBlank;

public class AddToWishlistRequest {

    @NotBlank(message = "Product ID is required")
    private String productId;

    public AddToWishlistRequest() {
    }

    public AddToWishlistRequest(String productId) {
        this.productId = productId;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }
}