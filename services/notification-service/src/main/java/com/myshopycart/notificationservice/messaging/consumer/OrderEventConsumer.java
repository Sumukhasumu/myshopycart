package com.myshopycart.notificationservice.messaging.consumer;

import com.myshopycart.notificationservice.messaging.event.MyShopyCartEvent;
import com.myshopycart.notificationservice.messaging.idempotency.IdempotencyService;
import com.myshopycart.notificationservice.messaging.message.MyShopyCartMessage;
import com.myshopycart.notificationservice.messaging.producer.NotificationTaskProducer;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class OrderEventConsumer {

    public static final String ORDER_EVENTS_TOPIC =
            "myshopycart.order.events";

    private final NotificationTaskProducer notificationTaskProducer;

    private final IdempotencyService idempotencyService;

    @KafkaListener(
            topics = ORDER_EVENTS_TOPIC,
            groupId = "${myshopycart.kafka.notification-group-id:myshopycart-notification}",
            containerFactory = "notificationKafkaListenerContainerFactory"
    )
    public void consume(MyShopyCartEvent<?> event) {

        try {
            if (event == null) {
                throw new IllegalArgumentException(
                        "Received null Kafka ORDER event");
            }

            if (idempotencyService.alreadyProcessed(event.eventId())) {

                System.out.println(
                        "Duplicate Kafka event skipped: "
                                + "eventId=" + event.eventId()
                                + ", eventType=" + event.eventType()
                                + ", aggregateId=" + event.aggregateId());

                return;
            }

            System.out.println(
                    "Received Kafka event: "
                            + "eventId=" + event.eventId()
                            + ", eventType=" + event.eventType()
                            + ", aggregateId=" + event.aggregateId());

            if ("ORDER_CONFIRMED".equals(event.eventType())) {

                MyShopyCartMessage<Object> message =
                        new MyShopyCartMessage<>(
                                UUID.randomUUID().toString(),
                                "SEND_ORDER_CONFIRMATION",
                                1,
                                Instant.now(),
                                "notification-service",
                                event.correlationId(),
                                event.eventId(),
                                event.payload()
                        );

                notificationTaskProducer.publish(message);

                System.out.println(
                        "Published RabbitMQ notification task: "
                                + "messageId=" + message.messageId()
                                + ", messageType=" + message.messageType()
                                + ", aggregateId=" + event.aggregateId());
            }

            idempotencyService.markProcessed(
                    event.eventId(),
                    event.eventType(),
                    event.source()
            );

            System.out.println(
                    "Kafka event processed successfully: "
                            + "eventId=" + event.eventId());

        } catch (Exception e) {

            throw new IllegalStateException(
                    "Failed to process Kafka ORDER event",
                    e
            );
        }
    }
}