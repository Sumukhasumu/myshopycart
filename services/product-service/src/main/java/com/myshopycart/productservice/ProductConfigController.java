package com.myshopycart.productservice;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class ProductConfigController {

    @Value("${myshopycart.product.message}")
    private String message;

    @Value("${myshopycart.product.service-name}")
    private String serviceName;

    @GetMapping("/api/products/config")
    public Map<String, String> getProductConfig() {
        return Map.of(
                "message", message,
                "serviceName", serviceName
        );
    }
}