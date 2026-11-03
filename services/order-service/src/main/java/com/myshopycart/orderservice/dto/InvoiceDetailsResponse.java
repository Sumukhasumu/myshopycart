
package com.myshopycart.orderservice.dto;

import java.math.BigDecimal;
import java.util.List;

public record InvoiceDetailsResponse(
        String invoiceNumber,
        Long orderId,
        String issueDate,
        String customerName,
        String customerEmail,
        String customerPhone,
        String customerGstin,
        AddressResponse billingAddress,
        AddressResponse shippingAddress,
        String paymentMethod,
        BigDecimal subtotal,
        BigDecimal taxAmount,
        BigDecimal totalAmount,
        List<ItemResponse> items) {

    public record AddressResponse(
            String address,
            String city,
            String state,
            String postalCode,
            String country) {
    }

    public record ItemResponse(
            String productId,
            String productName,
            Integer quantity,
            BigDecimal unitPrice,
            BigDecimal lineTotal) {
    }
}
