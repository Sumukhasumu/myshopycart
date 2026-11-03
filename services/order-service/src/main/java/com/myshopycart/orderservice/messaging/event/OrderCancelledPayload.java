package com.myshopycart.orderservice.messaging.event;

import java.math.BigDecimal;

public record OrderCancelledPayload(
        Long orderId,
        Long userId,
        BigDecimal totalAmount) {
}