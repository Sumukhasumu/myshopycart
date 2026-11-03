
package com.myshopycart.notificationservice.messaging.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.myshopycart.notificationservice.messaging.config.RabbitMqConfig;
import com.myshopycart.notificationservice.messaging.idempotency.IdempotencyService;
import com.myshopycart.notificationservice.messaging.message.MyShopyCartMessage;
import com.myshopycart.notificationservice.notification.OrderNotificationPayload;
import com.myshopycart.notificationservice.notification.NotificationDeliveryService;
import com.myshopycart.notificationservice.notification.OrderNotificationData;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class NotificationTaskConsumer {

    private static final String SEND_ORDER_CONFIRMATION =
            "SEND_ORDER_CONFIRMATION";

    private final IdempotencyService idempotencyService;
    private final NotificationDeliveryService notificationDeliveryService;
    private final ObjectMapper objectMapper;

    public NotificationTaskConsumer(
            IdempotencyService idempotencyService,
            NotificationDeliveryService notificationDeliveryService,
            ObjectMapper objectMapper) {

        this.idempotencyService = idempotencyService;
        this.notificationDeliveryService =
                notificationDeliveryService;
        this.objectMapper = objectMapper;
    }

    @RabbitListener(
            queues = "${myshopycart.messaging.notification-queue:"
                    + RabbitMqConfig.NOTIFICATION_QUEUE + "}",
            containerFactory = "notificationRabbitListenerContainerFactory"
    )
    public void consume(
            MyShopyCartMessage<?> message) {

        if (message == null) {

            throw new IllegalArgumentException(
                    "RabbitMQ notification message must not be null");
        }

        if (idempotencyService.alreadyProcessed(
                message.messageId())) {

            System.out.println(
                    "Duplicate RabbitMQ message skipped: "
                            + "messageId="
                            + message.messageId()
                            + ", messageType="
                            + message.messageType());

            return;
        }

        System.out.println(
                "Received RabbitMQ notification task: "
                        + "messageId="
                        + message.messageId()
                        + ", messageType="
                        + message.messageType()
                        + ", source="
                        + message.source()
                        + ", correlationId="
                        + message.correlationId()
                        + ", causationId="
                        + message.causationId());

        if (SEND_ORDER_CONFIRMATION.equals(
                message.messageType())) {

            OrderNotificationPayload payload =
                    objectMapper.convertValue(
                            message.payload(),
                            OrderNotificationPayload.class);

            OrderNotificationData data =
                    new OrderNotificationData(
                            payload.recipientEmail(),
                            payload.recipientMobile(),
                            payload.customerName(),
                            payload.orderId(),
                            payload.totalAmount(),
                            payload.invoiceNumber(),
                            payload.invoicePdf());

            notificationDeliveryService
                    .sendOrderConfirmation(
                            message.messageId(),
                            data);
        }

        idempotencyService.markProcessed(
                message.messageId(),
                message.messageType(),
                message.source());

        System.out.println(
                "RabbitMQ notification task processed successfully: "
                        + "messageId="
                        + message.messageId());
    }
}