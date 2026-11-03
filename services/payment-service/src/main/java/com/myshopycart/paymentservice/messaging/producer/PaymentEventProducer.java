package com.myshopycart.paymentservice.messaging.producer;

import com.myshopycart.paymentservice.messaging.event.MyShopyCartEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class PaymentEventProducer {

    public static final String PAYMENT_EVENTS_TOPIC =
            "myshopycart.payment.events";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public PaymentEventProducer(
            KafkaTemplate<String, Object> kafkaTemplate) {

        this.kafkaTemplate = kafkaTemplate;
    }

    public void publish(MyShopyCartEvent<?> event) {

        kafkaTemplate.send(
                PAYMENT_EVENTS_TOPIC,
                event.aggregateId(),
                event
        );
    }
}