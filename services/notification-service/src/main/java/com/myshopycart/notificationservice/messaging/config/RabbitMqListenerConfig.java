package com.myshopycart.notificationservice.messaging.config;

import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.backoff.FixedBackOffPolicy;
import org.springframework.retry.interceptor.RetryInterceptorBuilder;
import org.springframework.retry.policy.SimpleRetryPolicy;

@Configuration
public class RabbitMqListenerConfig {

    @Bean
    public SimpleRabbitListenerContainerFactory
    notificationRabbitListenerContainerFactory(
            ConnectionFactory connectionFactory,
            Jackson2JsonMessageConverter rabbitMessageConverter) {

        SimpleRabbitListenerContainerFactory factory =
                new SimpleRabbitListenerContainerFactory();

        factory.setConnectionFactory(connectionFactory);

        factory.setMessageConverter(rabbitMessageConverter);

        SimpleRetryPolicy retryPolicy =
                new SimpleRetryPolicy(3);

        FixedBackOffPolicy backOffPolicy =
                new FixedBackOffPolicy();

        backOffPolicy.setBackOffPeriod(500);

        factory.setAdviceChain(
                RetryInterceptorBuilder
                        .stateless()
                        .retryPolicy(retryPolicy)
                        .backOffPolicy(backOffPolicy)
                        .recoverer(
                                (args, cause) -> {
                                    throw new org.springframework.amqp
                                            .AmqpRejectAndDontRequeueException(
                                                    "Notification processing failed after 3 attempts",
                                                    cause
                                            );
                                }
                        )
                        .build()
        );

        return factory;
    }
}