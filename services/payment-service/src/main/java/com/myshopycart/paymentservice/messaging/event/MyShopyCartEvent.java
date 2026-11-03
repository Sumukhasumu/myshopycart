package com.myshopycart.paymentservice.messaging.event;

import java.time.Instant;

public record MyShopyCartEvent<T>(
        String eventId,
        String eventType,
        int eventVersion,
        Instant occurredAt,
        String source,
        String aggregateType,
        String aggregateId,
        String correlationId,
        String causationId,
        T payload) {
}