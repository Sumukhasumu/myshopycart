
package com.myshopycart.orderservice.messaging.producer;

import com.myshopycart.orderservice.messaging.event.MyShopyCartEvent;
import com.myshopycart.orderservice.messaging.event.OrderConfirmedPayload;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class OrderEventProducerTest {

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @InjectMocks
    private OrderEventProducer orderEventProducer;

    @Test
    void shouldPublishOrderConfirmedEvent() {

        OrderConfirmedPayload payload =
                new OrderConfirmedPayload(
                        999999L,
                        4L,
                        new BigDecimal("2000.00"));

        MyShopyCartEvent<OrderConfirmedPayload> event =
                new MyShopyCartEvent<>(
                        UUID.randomUUID().toString(),
                        "ORDER_CONFIRMED",
                        1,
                        Instant.now(),
                        "order-service",
                        "ORDER",
                        "999999",
                        "phase11-test-" + UUID.randomUUID(),
                        null,
                        payload);

        orderEventProducer.publish(event);

        verify(kafkaTemplate).send(
                OrderEventProducer.ORDER_EVENTS_TOPIC,
                event.aggregateId(),
                event);
    }
}
