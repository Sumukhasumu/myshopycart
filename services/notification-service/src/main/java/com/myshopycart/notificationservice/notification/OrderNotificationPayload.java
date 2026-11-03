package com.myshopycart.notificationservice.notification;

import com.fasterxml.jackson.annotation.JsonAlias;

import java.math.BigDecimal;

public record OrderNotificationPayload(
        Long orderId,
        Long userId,
        String customerName,

        @JsonAlias({"customerEmail", "recipientEmail"})
        String recipientEmail,

        @JsonAlias({"customerMobile", "recipientMobile"})
        String recipientMobile,

        BigDecimal totalAmount,
        String invoiceNumber,
        byte[] invoicePdf) {
}