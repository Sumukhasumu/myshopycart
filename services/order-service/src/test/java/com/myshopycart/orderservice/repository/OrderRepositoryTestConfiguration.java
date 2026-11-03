package com.myshopycart.orderservice.repository;

import com.myshopycart.orderservice.config.MinioConfig;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

import io.minio.MinioClient;

@TestConfiguration
@Profile("test")
public class OrderRepositoryTestConfiguration {

    @Bean
    @Primary
    public MinioClient testMinioClient() {
        return MinioClient.builder()
                .endpoint("http://localhost:9100")
                .credentials("myshopycart", "myshopycart")
                .build();
    }
}