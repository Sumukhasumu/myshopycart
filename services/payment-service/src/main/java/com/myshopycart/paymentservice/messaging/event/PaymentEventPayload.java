package com.myshopycart.paymentservice.messaging.event;

import java.math.BigDecimal;

public record PaymentEventPayload(
        Long paymentId,
        Long orderId,
        Long userId,
        BigDecimal amount,
        String status,
        String gatewayReference,
        String failureReason) {
}