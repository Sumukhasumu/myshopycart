package com.myshopycart.orderservice.service;

import com.myshopycart.orderservice.entity.InvoiceAddress;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record InvoicePdfData(
        String invoiceNumber,
        LocalDate issueDate,
        String customerName,
        String customerEmail,
        String customerPhone,
        String customerGstin,
        InvoiceAddress billingAddress,
        InvoiceAddress shippingAddress,
        String paymentMethod,
        BigDecimal subtotal,
        BigDecimal taxAmount,
        BigDecimal totalAmount,
        List<InvoicePdfItem> items) {

    public record InvoicePdfItem(
            String productId,
            String productName,
            Integer quantity,
            BigDecimal unitPrice,
            BigDecimal lineTotal) {
    }
}