package com.myshopycart.paymentservice.dto;

import com.myshopycart.paymentservice.entity.Payment;
import com.myshopycart.paymentservice.entity.PaymentMethod;
import com.myshopycart.paymentservice.entity.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PaymentResponse(
        Long id,
        Long orderId,
        Long userId,
        BigDecimal amount,
        PaymentStatus status,
        String idempotencyKey,
        PaymentMethod paymentMethod,
        String gatewayReference,
        String failureReason,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {

    public static PaymentResponse from(Payment payment) {

        PaymentMethod paymentMethod;

        try {
            paymentMethod = PaymentMethod.valueOf(
                    payment.getPaymentMethod());
        } catch (IllegalArgumentException |
                 NullPointerException exception) {
            paymentMethod = null;
        }

        return new PaymentResponse(
                payment.getId(),
                payment.getOrderId(),
                payment.getUserId(),
                payment.getAmount(),
                payment.getStatus(),
                payment.getIdempotencyKey(),
                paymentMethod,
                payment.getGatewayReference(),
                payment.getFailureReason(),
                payment.getCreatedAt(),
                payment.getUpdatedAt()
        );
    }
}