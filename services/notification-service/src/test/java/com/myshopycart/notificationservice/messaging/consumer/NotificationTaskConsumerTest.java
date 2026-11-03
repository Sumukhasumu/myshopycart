package com.myshopycart.notificationservice.messaging.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.myshopycart.notificationservice.messaging.idempotency.IdempotencyService;
import com.myshopycart.notificationservice.messaging.message.MyShopyCartMessage;
import com.myshopycart.notificationservice.notification.NotificationDeliveryService;
import com.myshopycart.notificationservice.notification.OrderNotificationData;
import com.myshopycart.notificationservice.notification.OrderNotificationPayload;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationTaskConsumerTest {

    @Mock
    private IdempotencyService idempotencyService;

    @Mock
    private NotificationDeliveryService
            notificationDeliveryService;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private NotificationTaskConsumer
            notificationTaskConsumer;

    @Test
    void shouldProcessNewRabbitMessage() {

        MyShopyCartMessage<Object> message =
                new MyShopyCartMessage<>(
                        "message-001",
                        "SEND_ORDER_CONFIRMATION",
                        1,
                        Instant.now(),
                        "notification-service",
                        "correlation-001",
                        "causation-001",
                        "order-payload"
                );

        OrderNotificationPayload payload =
                new OrderNotificationPayload(
                        101L,
                        25L,
                        "Test Customer",
                        "customer@example.com",
                        "9876543210",
                        new BigDecimal("2000.00"),
                        "INV-TEST-000001",
                        "TEST-PDF".getBytes()
                );

        when(idempotencyService.alreadyProcessed(
                "message-001"))
                .thenReturn(false);

        when(objectMapper.convertValue(
                any(),
                eq(OrderNotificationPayload.class)))
                .thenReturn(payload);

        notificationTaskConsumer.consume(message);

        verify(idempotencyService)
                .alreadyProcessed("message-001");

        verify(objectMapper)
                .convertValue(
                        any(),
                        eq(OrderNotificationPayload.class));

        verify(notificationDeliveryService)
                .sendOrderConfirmation(
                        eq("message-001"),
                        any(OrderNotificationData.class));

        verify(idempotencyService)
                .markProcessed(
                        "message-001",
                        "SEND_ORDER_CONFIRMATION",
                        "notification-service"
                );
    }

    @Test
    void shouldSkipDuplicateRabbitMessage() {

        MyShopyCartMessage<Object> message =
                new MyShopyCartMessage<>(
                        "message-001",
                        "SEND_ORDER_CONFIRMATION",
                        1,
                        Instant.now(),
                        "notification-service",
                        "correlation-001",
                        "causation-001",
                        "order-payload"
                );

        when(idempotencyService.alreadyProcessed(
                "message-001"))
                .thenReturn(true);

        notificationTaskConsumer.consume(message);

        verify(idempotencyService)
                .alreadyProcessed("message-001");

        verify(objectMapper, never())
                .convertValue(
                        any(),
                        eq(OrderNotificationPayload.class));

        verify(notificationDeliveryService, never())
                .sendOrderConfirmation(
                        any(),
                        any(OrderNotificationData.class));

        verify(idempotencyService, never())
                .markProcessed(
                        "message-001",
                        "SEND_ORDER_CONFIRMATION",
                        "notification-service"
                );
    }
}