package com.myshopycart.authservice.controller;

import com.myshopycart.authservice.service.ProductDiscoveryService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ProductDiscoveryController {

    private final ProductDiscoveryService productDiscoveryService;

    public ProductDiscoveryController(ProductDiscoveryService productDiscoveryService) {
        this.productDiscoveryService = productDiscoveryService;
    }

    @GetMapping("/api/auth/discover-product")
    public String discoverProduct() {
        return productDiscoveryService.getProductHello();
    }
}