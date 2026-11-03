package com.myshopycart.orderservice.messaging.producer;

import com.myshopycart.orderservice.messaging.event.MyShopyCartEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class OrderEventProducer {

    public static final String ORDER_EVENTS_TOPIC =
            "myshopycart.order.events";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public OrderEventProducer(
            KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publish(MyShopyCartEvent<?> event) {

        kafkaTemplate.send(
                ORDER_EVENTS_TOPIC,
                event.aggregateId(),
                event
        );
    }
}