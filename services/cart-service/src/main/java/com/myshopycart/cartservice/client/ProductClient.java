package com.myshopycart.cartservice.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class ProductClient {

    private final RestClient restClient;

    public ProductClient(
            RestClient.Builder restClientBuilder,
            @Value("${product-service.base-url:http://localhost:8082}")
            String productServiceBaseUrl) {

        this.restClient = restClientBuilder
                .baseUrl(productServiceBaseUrl)
                .build();
    }

    public boolean productExists(String productId) {

        try {
            restClient.get()
                    .uri("/api/products/{productId}", productId)
                    .retrieve()
                    .toBodilessEntity();

            return true;

        } catch (Exception ex) {
            return false;
        }
    }
}