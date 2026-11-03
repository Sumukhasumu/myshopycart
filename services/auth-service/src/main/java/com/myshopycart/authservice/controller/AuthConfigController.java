package com.myshopycart.authservice.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class AuthConfigController {

    @Value("${myshopycart.auth.message}")
    private String message;

    @Value("${myshopycart.auth.service-name}")
    private String serviceName;

    @GetMapping("/api/auth/config")
    public Map<String, String> getAuthConfig() {
        return Map.of(
                "message", message,
                "serviceName", serviceName
        );
    }
}