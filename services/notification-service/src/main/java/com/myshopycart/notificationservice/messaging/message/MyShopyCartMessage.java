package com.myshopycart.notificationservice.messaging.message;

import java.time.Instant;

public record MyShopyCartMessage<T>(
        String messageId,
        String messageType,
        int messageVersion,
        Instant createdAt,
        String source,
        String correlationId,
        String causationId,
        T payload) {
}