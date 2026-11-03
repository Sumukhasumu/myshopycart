
package com.myshopycart.gateway;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.MOCK,
    properties = {
        "spring.cloud.config.enabled=false",
        "eureka.client.enabled=false",
        "spring.cloud.discovery.enabled=false",
        "spring.cloud.gateway.enabled=false"
    }
)
@TestPropertySource(properties = {
    "server.port=0"
})
class ApiGatewayApplicationTests {

    @Test
    void contextLoads() {
        // Passing this test confirms that the application context starts.
    }
}
