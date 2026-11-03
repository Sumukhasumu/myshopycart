package com.myshopycart.authservice.client;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class ProductClient {

    private final RestTemplate restTemplate;

    public ProductClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public String getProductHello() {

        return restTemplate.getForObject(
                "http://product-service/products/hello",
                String.class
        );
    }
}