package com.myshopycart.orderservice.service;

import com.myshopycart.orderservice.entity.InvoiceAddress;

public record InvoiceCustomerSnapshot(
        String customerName,
        String customerEmail,
        String customerPhone,
        String customerGstin,
        InvoiceAddress billingAddress,
        InvoiceAddress shippingAddress) {
}