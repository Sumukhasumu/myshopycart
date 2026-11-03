package com.myshopycart.cartservice.dto;

import java.util.List;

public class CartResponse {

    private Long cartId;
    private Long userId;
    private List<CartItemResponse> items;

    public CartResponse() {
    }

    public CartResponse(
            Long cartId,
            Long userId,
            List<CartItemResponse> items) {

        this.cartId = cartId;
        this.userId = userId;
        this.items = items;
    }

    public Long getCartId() {
        return cartId;
    }

    public void setCartId(Long cartId) {
        this.cartId = cartId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public List<CartItemResponse> getItems() {
        return items;
    }

    public void setItems(List<CartItemResponse> items) {
        this.items = items;
    }
}