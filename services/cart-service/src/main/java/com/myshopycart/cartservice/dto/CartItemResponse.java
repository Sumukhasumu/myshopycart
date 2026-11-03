package com.myshopycart.cartservice.dto;

public class CartItemResponse {

    private Long cartItemId;
    private Long cartId;
    private String productId;
    private Integer quantity;

    public CartItemResponse() {
    }

    public CartItemResponse(
            Long cartItemId,
            Long cartId,
            String productId,
            Integer quantity) {

        this.cartItemId = cartItemId;
        this.cartId = cartId;
        this.productId = productId;
        this.quantity = quantity;
    }

    public Long getCartItemId() {
        return cartItemId;
    }

    public void setCartItemId(Long cartItemId) {
        this.cartItemId = cartItemId;
    }

    public Long getCartId() {
        return cartId;
    }

    public void setCartId(Long cartId) {
        this.cartId = cartId;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }
}