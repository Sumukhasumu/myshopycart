package com.myshopycart.notificationservice.messaging.producer;

import com.myshopycart.notificationservice.messaging.config.RabbitMqConfig;
import com.myshopycart.notificationservice.messaging.message.MyShopyCartMessage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class NotificationTaskProducerTest {

    @Mock
    private RabbitTemplate rabbitTemplate;

    @Test
    void shouldPublishMessageToNotificationExchange() {

        NotificationTaskProducer producer =
                new NotificationTaskProducer(rabbitTemplate);

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

        producer.publish(message);

        ArgumentCaptor<String> exchangeCaptor =
                ArgumentCaptor.forClass(String.class);

        ArgumentCaptor<String> routingKeyCaptor =
                ArgumentCaptor.forClass(String.class);

        ArgumentCaptor<Object> messageCaptor =
                ArgumentCaptor.forClass(Object.class);

        verify(rabbitTemplate).convertAndSend(
                exchangeCaptor.capture(),
                routingKeyCaptor.capture(),
                messageCaptor.capture()
        );

        assertSame(
                RabbitMqConfig.NOTIFICATION_EXCHANGE,
                exchangeCaptor.getValue()
        );

        assertSame(
                RabbitMqConfig.NOTIFICATION_ROUTING_KEY,
                routingKeyCaptor.getValue()
        );

        assertSame(
                message,
                messageCaptor.getValue()
        );
    }
}