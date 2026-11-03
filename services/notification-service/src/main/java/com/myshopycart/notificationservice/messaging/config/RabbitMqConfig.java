
package com.myshopycart.notificationservice.messaging.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqConfig {

    public static final String NOTIFICATION_EXCHANGE =
            "myshopycart.notification.exchange";

    public static final String NOTIFICATION_QUEUE =
            "myshopycart.notification.queue";

    public static final String NOTIFICATION_ROUTING_KEY =
            "notification.order.confirmation";

    public static final String NOTIFICATION_DLX =
            "myshopycart.notification.dlx";

    public static final String NOTIFICATION_DLQ =
            "myshopycart.notification.dlq";

    public static final String NOTIFICATION_DEAD_ROUTING_KEY =
            "notification.dead";

    public static final String TEST_NOTIFICATION_QUEUE =
            "myshopycart.notification.test.queue";

    public static final String TEST_NOTIFICATION_ROUTING_KEY =
            "notification.order.confirmation.test";

    @Value("${myshopycart.messaging.notification-queue:"
            + NOTIFICATION_QUEUE + "}")
    private String notificationQueueName;

    @Value("${myshopycart.messaging.notification-routing-key:"
            + NOTIFICATION_ROUTING_KEY + "}")
    private String notificationRoutingKey;

    @Bean
    public DirectExchange notificationExchange() {
        return new DirectExchange(
                NOTIFICATION_EXCHANGE,
                true,
                false
        );
    }

    @Bean
    public DirectExchange notificationDeadLetterExchange() {
        return new DirectExchange(
                NOTIFICATION_DLX,
                true,
                false
        );
    }

    @Bean
    public Queue notificationQueue() {
        return QueueBuilder
                .durable(notificationQueueName)
                .deadLetterExchange(NOTIFICATION_DLX)
                .deadLetterRoutingKey(NOTIFICATION_DEAD_ROUTING_KEY)
                .build();
    }

    @Bean
    public Queue notificationDeadLetterQueue() {
        return QueueBuilder
                .durable(NOTIFICATION_DLQ)
                .build();
    }

    @Bean
    public Binding notificationQueueBinding(
            Queue notificationQueue,
            DirectExchange notificationExchange) {

        return BindingBuilder
                .bind(notificationQueue)
                .to(notificationExchange)
                .with(notificationRoutingKey);
    }

    @Bean
    public Binding notificationDeadLetterQueueBinding(
            Queue notificationDeadLetterQueue,
            DirectExchange notificationDeadLetterExchange) {

        return BindingBuilder
                .bind(notificationDeadLetterQueue)
                .to(notificationDeadLetterExchange)
                .with(NOTIFICATION_DEAD_ROUTING_KEY);
    }

    @Bean
    public Jackson2JsonMessageConverter rabbitMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(
            ConnectionFactory connectionFactory,
            Jackson2JsonMessageConverter rabbitMessageConverter) {

        RabbitTemplate rabbitTemplate =
                new RabbitTemplate(connectionFactory);

        rabbitTemplate.setMessageConverter(rabbitMessageConverter);

        return rabbitTemplate;
    }
}