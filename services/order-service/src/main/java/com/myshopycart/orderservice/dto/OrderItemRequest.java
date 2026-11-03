package com.myshopycart.orderservice.dto;

import java.math.BigDecimal;

public record OrderItemRequest(
        String productId,
        Integer quantity,
        BigDecimal price
) {
}