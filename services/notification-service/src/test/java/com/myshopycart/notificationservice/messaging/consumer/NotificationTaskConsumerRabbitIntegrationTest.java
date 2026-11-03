package com.myshopycart.notificationservice.messaging.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.myshopycart.notificationservice.messaging.message.MyShopyCartMessage;
import com.myshopycart.notificationservice.notification.OrderNotificationPayload;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@TestPropertySource(properties = {
        "spring.rabbitmq.host=host.docker.internal",
        "spring.rabbitmq.port=5672",
        "spring.rabbitmq.username=myshopycart",
        "spring.rabbitmq.password=MyShopyCart@123"
})
class NotificationTaskConsumerRabbitIntegrationTest {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private RabbitAdmin rabbitAdmin;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldPreserveInvoicePdfBytesAcrossRabbitMq()
            throws Exception {

        String suffix =
                UUID.randomUUID().toString();

        String exchangeName =
                "myshopycart.test.exchange."
                        + suffix;

        String queueName =
                "myshopycart.test.queue."
                        + suffix;

        String routingKey =
                "test.notification."
                        + suffix;

        try {

            DirectExchange exchange =
                    new DirectExchange(
                            exchangeName,
                            false,
                            true);

            Queue queue =
        		new Queue(
                	queueName,
                	false,
                	true,
                	true);

            Binding binding =
                    BindingBuilder
                            .bind(queue)
                            .to(exchange)
                            .with(routingKey);

            rabbitAdmin.declareExchange(exchange);

            rabbitAdmin.declareQueue(queue);

            rabbitAdmin.declareBinding(binding);

            byte[] originalInvoicePdf =
                    new byte[] {
                            '%', 'P', 'D', 'F', '-',
                            '1', '.', '7', '\n',
                            0x25, (byte) 0xE2,
                            0x28, (byte) 0xA1,
                            0x00, 0x01, 0x7F,
                            0x10, 0x20, 0x30, 0x40,
                            'R', 'A', 'B', 'B', 'I', 'T',
                            '\n'
                    };

            OrderNotificationPayload payload =
                    new OrderNotificationPayload(
                            2001L,
                            25L,
                            "Rabbit Integration Customer",
                            "rabbit@example.com",
                            "9876543210",
                            new BigDecimal("2600.00"),
                            "INV-RABBIT-000001",
                            originalInvoicePdf
                    );

            MyShopyCartMessage<
                    OrderNotificationPayload> message =
                    new MyShopyCartMessage<>(
                            "rabbit-integration-"
                                    + UUID.randomUUID(),
                            "SEND_ORDER_CONFIRMATION",
                            1,
                            Instant.now(),
                            "notification-test",
                            "rabbit-correlation-"
                                    + UUID.randomUUID(),
                            "rabbit-causation-"
                                    + UUID.randomUUID(),
                            payload
                    );

            rabbitTemplate.convertAndSend(
                    exchangeName,
                    routingKey,
                    message
            );

            Object received =
                    rabbitTemplate.receiveAndConvert(
                            queueName,
                            15000
                    );

            assertNotNull(
                    received,
                    "RabbitMQ message was not received"
            );

            MyShopyCartMessage<?> receivedMessage =
                    (MyShopyCartMessage<?>) received;

            assertEquals(
                    "SEND_ORDER_CONFIRMATION",
                    receivedMessage.messageType()
            );

            assertEquals(
                    message.messageId(),
                    receivedMessage.messageId()
            );

            OrderNotificationPayload
                    receivedPayload =
                    objectMapper.convertValue(
                            receivedMessage.payload(),
                            OrderNotificationPayload.class
                    );

            assertNotNull(
                    receivedPayload,
                    "Notification payload must not be null"
            );

            assertEquals(
                    "rabbit@example.com",
                    receivedPayload.recipientEmail()
            );

            assertEquals(
                    "9876543210",
                    receivedPayload.recipientMobile()
            );

            assertEquals(
                    2001L,
                    receivedPayload.orderId()
            );

            assertEquals(
        	0,
        	new BigDecimal("2600.00")
                .compareTo(receivedPayload.totalAmount()),
        	"Total amount value must be preserved across RabbitMQ"
	);

            assertNotNull(
                    receivedPayload.invoicePdf(),
                    "Invoice PDF bytes must not be null"
            );

            assertArrayEquals(
                    originalInvoicePdf,
                    receivedPayload.invoicePdf(),
                    "Invoice PDF bytes changed while passing through RabbitMQ"
            );

        } finally {

            rabbitAdmin.deleteQueue(
                    queueName);

            rabbitAdmin.deleteExchange(
                    exchangeName);
        }
    }
}