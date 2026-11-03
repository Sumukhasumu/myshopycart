package com.myshopycart.orderservice.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;

@Component
public class ProductClient {

    private final RestClient restClient;

    public ProductClient(
            RestClient.Builder restClientBuilder,
            @Value("${product-service.base-url}") String productServiceBaseUrl) {

        this.restClient = restClientBuilder
                .baseUrl(productServiceBaseUrl)
                .build();
    }

    public ProductResponse getProduct(
            String productId,
            String authorizationHeader) {

        return restClient
                .get()
                .uri("/api/products/{productId}", productId)
                .header(
                        HttpHeaders.AUTHORIZATION,
                        authorizationHeader)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .body(ProductResponse.class);
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ProductResponse(
            String id,
            String name,
            BigDecimal price) {
    }
}