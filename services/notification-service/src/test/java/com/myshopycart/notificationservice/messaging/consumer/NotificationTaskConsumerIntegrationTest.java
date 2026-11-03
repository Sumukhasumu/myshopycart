package com.myshopycart.notificationservice.messaging.consumer;

import com.myshopycart.notificationservice.messaging.config.RabbitMqConfig;
import com.myshopycart.notificationservice.messaging.idempotency.ProcessedMessage;
import com.myshopycart.notificationservice.messaging.idempotency.ProcessedMessageRepository;
import com.myshopycart.notificationservice.messaging.message.MyShopyCartMessage;
import com.myshopycart.notificationservice.notification.NotificationDeliveryService;
import com.myshopycart.notificationservice.notification.OrderNotificationPayload;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;

@SpringBootTest(properties = {
        "myshopycart.messaging.notification-queue=myshopycart.notification.test.queue",
        "myshopycart.messaging.notification-routing-key=notification.order.confirmation.test"
})
class NotificationTaskConsumerIntegrationTest {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private ProcessedMessageRepository processedMessageRepository;

    @MockBean
    private NotificationDeliveryService notificationDeliveryService;

    @Test
    void shouldConsumeRabbitMessageAndPersistIdempotencyRecord()
            throws InterruptedException {

        String messageId =
                "rabbit-integration-" + UUID.randomUUID();

        OrderNotificationPayload payload =
                new OrderNotificationPayload(
                        999L,
                        4L,
                        "Test Customer",
                        "test@example.com",
                        "1234567890",
                        BigDecimal.valueOf(1000),
                        "INV-TEST-000001",
                        new byte[]{1, 2, 3, 4}
                );

        MyShopyCartMessage<OrderNotificationPayload> message =
                new MyShopyCartMessage<>(
                        messageId,
                        "SEND_ORDER_CONFIRMATION",
                        1,
                        Instant.now(),
                        "notification-service-test",
                        UUID.randomUUID().toString(),
                        UUID.randomUUID().toString(),
                        payload
                );

        doNothing()
                .when(notificationDeliveryService)
                .sendOrderConfirmation(
                        any(String.class),
                        any()
                );

        rabbitTemplate.convertAndSend(
                RabbitMqConfig.NOTIFICATION_EXCHANGE,
                RabbitMqConfig.TEST_NOTIFICATION_ROUTING_KEY,
                message
        );

        long deadline =
                System.currentTimeMillis()
                        + Duration.ofSeconds(10).toMillis();

        ProcessedMessage processedMessage = null;

        while (System.currentTimeMillis() < deadline) {

            processedMessage =
                    processedMessageRepository
                            .findById(messageId)
                            .orElse(null);

            if (processedMessage != null) {
                break;
            }

            Thread.sleep(200);
        }

        assertNotNull(
                processedMessage,
                "RabbitMQ message was not consumed and persisted within 10 seconds"
        );

        assertTrue(
                processedMessage.getMessageId().equals(messageId)
        );

        assertTrue(
                processedMessage.getMessageType()
                        .equals("SEND_ORDER_CONFIRMATION")
        );

        assertTrue(
                processedMessage.getSource()
                        .equals("notification-service-test")
        );

        processedMessageRepository.deleteById(messageId);
    }
}