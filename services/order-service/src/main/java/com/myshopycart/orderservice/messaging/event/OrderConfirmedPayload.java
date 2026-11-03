package com.myshopycart.orderservice.messaging.event;

import java.math.BigDecimal;

public record OrderConfirmedPayload(
        Long orderId,
        Long userId,
        String customerName,
        String customerEmail,
        String customerMobile,
        BigDecimal totalAmount,
        String invoiceNumber,
        byte[] invoicePdf) {

    /*
     * Backward-compatible constructor for existing tests.
     */
    public OrderConfirmedPayload(
            Long orderId,
            Long userId,
            BigDecimal totalAmount) {

        this(
                orderId,
                userId,
                null,
                null,
                null,
                totalAmount,
                null,
                null);
    }
}