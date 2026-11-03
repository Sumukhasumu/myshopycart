package com.myshopycart.orderservice.dto;

import com.myshopycart.orderservice.entity.OrderStatus;

public record UpdateOrderStatusRequest(
        OrderStatus status
) {
}