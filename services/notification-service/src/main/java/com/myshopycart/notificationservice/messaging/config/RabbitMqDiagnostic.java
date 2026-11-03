package com.myshopycart.notificationservice.messaging.config;

import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.rabbit.connection.Connection;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqDiagnostic {

    @Bean
    CommandLineRunner rabbitMqDiagnosticRunner(
            ConnectionFactory connectionFactory,
            AmqpAdmin amqpAdmin) {

        return args -> {

            System.out.println("========== RABBITMQ DIAGNOSTIC ==========");

            System.out.println(
                    "RabbitMQ host: "
                            + connectionFactory.getHost());

            System.out.println(
                    "RabbitMQ port: "
                            + connectionFactory.getPort());

            System.out.println(
                    "RabbitMQ username: "
                            + connectionFactory.getUsername());

            System.out.println("Opening RabbitMQ connection...");

            try (Connection connection =
                         connectionFactory.createConnection()) {

                System.out.println(
                        "RabbitMQ connection OPEN: "
                                + connection.isOpen());

                System.out.println(
                        "RabbitMQ connection local port: "
                                + connection.getLocalPort());

                System.out.println(
                        "Declaring RabbitMQ objects...");

                amqpAdmin.initialize();

                System.out.println(
                        "RabbitMQ declarations completed successfully.");

            } catch (Exception e) {

                System.out.println(
                        "RabbitMQ diagnostic FAILED:");

                e.printStackTrace();
            }

            System.out.println("==========================================");
        };
    }
}