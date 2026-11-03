package com.myshopycart.orderservice.dto;

import java.util.List;

public record CreateOrderRequest(
        List<OrderItemRequest> items,
        Long shippingAddressId,
        Long billingAddressId
) {

    public CreateOrderRequest(List<OrderItemRequest> items) {
        this(items, null, null);
    }
}