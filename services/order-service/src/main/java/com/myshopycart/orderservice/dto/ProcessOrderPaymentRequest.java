package com.myshopycart.orderservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record ProcessOrderPaymentRequest(

        @NotBlank(message = "Payment method is required")
        @Pattern(
                regexp = "UPI|CARD|DIGITAL_WALLET",
                message = "Payment method must be UPI, CARD, or DIGITAL_WALLET"
        )
        String paymentMethod,

        @NotBlank(message = "Simulation mode is required")
        @Pattern(
                regexp = "SUCCESS|FAILURE",
                message = "Simulation mode must be SUCCESS or FAILURE"
        )
        String simulationMode
) {
}