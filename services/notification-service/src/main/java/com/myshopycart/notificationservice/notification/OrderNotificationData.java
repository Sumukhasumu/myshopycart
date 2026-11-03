package com.myshopycart.notificationservice.notification;

import java.math.BigDecimal;

public record OrderNotificationData(
        String recipientEmail,
        String recipientMobile,
        String customerName,
        Long orderId,
        BigDecimal totalAmount,
        String invoiceNumber,
        byte[] invoicePdf) {
}