package com.myshopycart.notificationservice.messaging.config;

import com.myshopycart.notificationservice.messaging.event.MyShopyCartEvent;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;

@Configuration
public class KafkaConfig {

    @Bean
    public ConsumerFactory<String, MyShopyCartEvent> notificationKafkaConsumerFactory(
            KafkaProperties kafkaProperties) {

        JsonDeserializer<MyShopyCartEvent> jsonDeserializer =
                new JsonDeserializer<>(MyShopyCartEvent.class, false);

        jsonDeserializer.addTrustedPackages(
                "com.myshopycart.notificationservice.messaging",
                "com.myshopycart.orderservice.messaging"
        );

        return new DefaultKafkaConsumerFactory<>(
                kafkaProperties.buildConsumerProperties(),
                new StringDeserializer(),
                jsonDeserializer
        );
    }

    @Bean(name = "notificationKafkaListenerContainerFactory")
    public ConcurrentKafkaListenerContainerFactory<String, MyShopyCartEvent>
    notificationKafkaListenerContainerFactory(
            ConsumerFactory<String, MyShopyCartEvent> notificationKafkaConsumerFactory) {

        ConcurrentKafkaListenerContainerFactory<String, MyShopyCartEvent> factory =
                new ConcurrentKafkaListenerContainerFactory<>();

        factory.setConsumerFactory(notificationKafkaConsumerFactory);

        return factory;
    }
}