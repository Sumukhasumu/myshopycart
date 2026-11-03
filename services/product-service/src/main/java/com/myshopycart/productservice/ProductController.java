package com.myshopycart.productservice;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ProductController {

    @GetMapping("/products/hello")
    public String hello() {
        return "Hello from Product Service";
    }
}