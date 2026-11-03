
package com.myshopycart.gateway;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.yaml.snakeyaml.Yaml;

class ApiGatewayRouteConfigurationTest {

    @Test
    void shouldDefineAllSixExpectedRoutes() throws Exception {
        Map<String, Object> configuration = loadConfiguration();

        Map<?, ?> spring = (Map<?, ?>) configuration.get("spring");
        Map<?, ?> cloud = (Map<?, ?>) spring.get("cloud");
        Map<?, ?> gateway = (Map<?, ?>) cloud.get("gateway");

        List<?> routes = (List<?>) gateway.get("routes");

        assertEquals(6, routes.size());

        Map<String, String> expectedRoutes = new java.util.HashMap<>(Map.of(
    		"auth-service", "lb://auth-service",
    		"product-service", "lb://product-service",
    		"inventory-service", "lb://inventory-service",
    		"cart-service", "lb://cart-service",
    		"wishlist-service", "lb://cart-service",
   		"order-service", "lb://order-service"
		));

        for (Object routeObject : routes) {
            Map<?, ?> route = (Map<?, ?>) routeObject;

            String routeId = (String) route.get("id");
            String expectedUri = expectedRoutes.get(routeId);

            assertTrue(expectedUri != null, "Unexpected route: " + routeId);
            assertEquals(expectedUri, route.get("uri"));
            expectedRoutes.remove(routeId);
        }

        assertTrue(expectedRoutes.isEmpty(),
            "Missing routes: " + expectedRoutes.keySet());
    }

    @Test
    void shouldConfigureFrontendCorsOriginAndMethods() throws Exception {
        Map<String, Object> configuration = loadConfiguration();

        Map<?, ?> spring = (Map<?, ?>) configuration.get("spring");
        Map<?, ?> cloud = (Map<?, ?>) spring.get("cloud");
        Map<?, ?> gateway = (Map<?, ?>) cloud.get("gateway");
        Map<?, ?> globalCors = (Map<?, ?>) gateway.get("globalcors");
        Map<?, ?> corsConfigurations =
            (Map<?, ?>) globalCors.get("cors-configurations");
        Map<?, ?> cors = (Map<?, ?>) corsConfigurations.get("[/**]");

        assertEquals(
            List.of("http://localhost:5173"),
            cors.get("allowedOrigins")
        );

        assertEquals(
            List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"),
            cors.get("allowedMethods")
        );

        assertEquals(Boolean.TRUE, cors.get("allowCredentials"));
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> loadConfiguration() throws Exception {
        try (InputStream input = getClass()
                .getClassLoader()
                .getResourceAsStream("api-gateway-test.yml")) {

            if (input == null) {
                throw new IllegalStateException(
                    "api-gateway-test.yml was not found on the test classpath"
                );
            }

            return new Yaml().load(input);
        }
    }
}
