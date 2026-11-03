package com.myshopycart.orderservice.service;

public record InvoiceDownloadData(
        String invoiceNumber,
        byte[] pdfBytes) {
}
