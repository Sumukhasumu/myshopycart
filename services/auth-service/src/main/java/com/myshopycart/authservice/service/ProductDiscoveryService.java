package com.myshopycart.authservice.service;

import com.myshopycart.authservice.client.ProductClient;
import org.springframework.stereotype.Service;

@Service
public class ProductDiscoveryService {

    private final ProductClient productClient;

    public ProductDiscoveryService(ProductClient productClient) {
        this.productClient = productClient;
    }

    public String getProductHello() {
        return productClient.getProductHello();
    }
}