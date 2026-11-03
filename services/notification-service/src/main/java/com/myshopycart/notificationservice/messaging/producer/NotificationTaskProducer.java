package com.myshopycart.notificationservice.messaging.producer;

import com.myshopycart.notificationservice.messaging.config.RabbitMqConfig;
import com.myshopycart.notificationservice.messaging.message.MyShopyCartMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NotificationTaskProducer {

    private final RabbitTemplate rabbitTemplate;

    public void publish(
            MyShopyCartMessage<?> message) {

        rabbitTemplate.convertAndSend(
                RabbitMqConfig.NOTIFICATION_EXCHANGE,
                RabbitMqConfig.NOTIFICATION_ROUTING_KEY,
                message
        );
    }
}