package com.myshopycart.inventoryservice.dto;

public record AdjustStockRequest(
        Integer quantity,
        String operation,
        String reason) {
}