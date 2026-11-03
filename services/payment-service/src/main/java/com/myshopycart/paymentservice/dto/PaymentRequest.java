package com.myshopycart.paymentservice.dto;

import com.myshopycart.paymentservice.entity.PaymentMethod;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record PaymentRequest(

        @NotNull(message = "Order ID is required")
        @Positive(message = "Order ID must be positive")
        Long orderId,

        @NotNull(message = "User ID is required")
        @Positive(message = "User ID must be positive")
        Long userId,

        @NotNull(message = "Amount is required")
        @DecimalMin(
                value = "0.01",
                message = "Amount must be greater than zero"
        )
        BigDecimal amount,

        @NotBlank(message = "Idempotency key is required")
        String idempotencyKey,

        @NotNull(message = "Payment method is required")
        PaymentMethod paymentMethod,

        String simulationMode
) {
}