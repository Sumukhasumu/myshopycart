package com.myshopycart.orderservice.messaging.event;

import java.math.BigDecimal;

public record OrderCreatedPayload(
        Long orderId,
        Long userId,
        BigDecimal totalAmount) {
}