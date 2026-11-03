
package com.myshopycart.authservice;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = {
        "spring.config.import=",
        "spring.datasource.url=jdbc:postgresql://host.docker.internal:5432/myshopycart_auth",
        "spring.datasource.username=postgres",
        "spring.datasource.password=postgres",
        "spring.jpa.hibernate.ddl-auto=none",
        "eureka.client.enabled=false",
        "myshopycart.auth.message=Auth Service test configuration",
        "myshopycart.auth.service-name=auth-service",
        "myshopycart.auth.jwt-secret=myshopycart-test-secret-key-for-jwt-signing",
        "myshopycart.auth.jwt-expiration=3600000"
})
class AuthServiceApplicationTests {

    @Test
    void contextLoads() {
    }
}