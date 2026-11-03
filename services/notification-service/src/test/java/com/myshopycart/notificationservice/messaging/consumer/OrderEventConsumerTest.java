package com.myshopycart.notificationservice.messaging.consumer;

import com.myshopycart.notificationservice.messaging.event.MyShopyCartEvent;
import com.myshopycart.notificationservice.messaging.idempotency.IdempotencyService;
import com.myshopycart.notificationservice.messaging.message.MyShopyCartMessage;
import com.myshopycart.notificationservice.messaging.producer.NotificationTaskProducer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderEventConsumerTest {

    @Mock
    private NotificationTaskProducer notificationTaskProducer;

    @Mock
    private IdempotencyService idempotencyService;

    @InjectMocks
    private OrderEventConsumer orderEventConsumer;

    @Test
    void shouldPublishRabbitTaskForOrderConfirmedEvent() {

        MyShopyCartEvent<Object> event =
                new MyShopyCartEvent<>(
                        "event-001",
                        "ORDER_CONFIRMED",
                        1,
                        Instant.now(),
                        "order-service",
                        "ORDER",
                        "22",
                        "correlation-001",
                        null,
                        "order-payload"
                );

        when(idempotencyService.alreadyProcessed("event-001"))
                .thenReturn(false);

        orderEventConsumer.consume(event);

        ArgumentCaptor<MyShopyCartMessage<?>> captor =
                ArgumentCaptor.forClass(MyShopyCartMessage.class);

        verify(notificationTaskProducer)
                .publish(captor.capture());

        MyShopyCartMessage<?> message = captor.getValue();

        assertEquals(
                "SEND_ORDER_CONFIRMATION",
                message.messageType()
        );

        assertEquals(
                "notification-service",
                message.source()
        );

        assertEquals(
                "correlation-001",
                message.correlationId()
        );

        assertEquals(
                "event-001",
                message.causationId()
        );

        verify(idempotencyService)
                .markProcessed(
                        "event-001",
                        "ORDER_CONFIRMED",
                        "order-service"
                );
    }

    @Test
    void shouldNotPublishRabbitTaskForNonConfirmedEvent() {

        MyShopyCartEvent<Object> event =
                new MyShopyCartEvent<>(
                        "event-002",
                        "ORDER_CREATED",
                        1,
                        Instant.now(),
                        "order-service",
                        "ORDER",
                        "22",
                        "correlation-002",
                        null,
                        "order-payload"
                );

        when(idempotencyService.alreadyProcessed("event-002"))
                .thenReturn(false);

        orderEventConsumer.consume(event);

        verify(notificationTaskProducer, never())
                .publish(org.mockito.ArgumentMatchers.any());

        verify(idempotencyService)
                .markProcessed(
                        "event-002",
                        "ORDER_CREATED",
                        "order-service"
                );
    }

    @Test
    void shouldSkipDuplicateKafkaEvent() {

        MyShopyCartEvent<Object> event =
                new MyShopyCartEvent<>(
                        "event-003",
                        "ORDER_CONFIRMED",
                        1,
                        Instant.now(),
                        "order-service",
                        "ORDER",
                        "22",
                        "correlation-003",
                        null,
                        "order-payload"
                );

        when(idempotencyService.alreadyProcessed("event-003"))
                .thenReturn(true);

        orderEventConsumer.consume(event);

        verify(notificationTaskProducer, never())
                .publish(org.mockito.ArgumentMatchers.any());

        verify(idempotencyService, never())
                .markProcessed(
                        "event-003",
                        "ORDER_CONFIRMED",
                        "order-service"
                );
    }
}