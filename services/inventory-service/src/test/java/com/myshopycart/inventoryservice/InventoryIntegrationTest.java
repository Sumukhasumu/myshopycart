package com.myshopycart.inventoryservice;

import com.myshopycart.inventoryservice.security.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class InventoryIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void shouldCompleteInventoryLifecycle() {

        String productId =
                "INT-" + System.currentTimeMillis();

        String token =
                jwtService.generateToken(
                        "integration-admin",
                        1L,
                        "ADMIN");

        HttpHeaders headers =
                createHeaders(token);

        // 1. Create inventory with 10 units
        ResponseEntity<String> createResponse =
                post(
                        "/api/inventory",
                        Map.of(
                                "productId", productId,
                                "availableStock", 10),
                        headers);

        assertEquals(
                HttpStatus.OK,
                createResponse.getStatusCode());

        // 2. Get inventory
        ResponseEntity<String> getResponse =
                get(
                        "/api/inventory/" + productId,
                        headers);

        assertEquals(
                HttpStatus.OK,
                getResponse.getStatusCode());

        // 3. Add 5 units
        ResponseEntity<String> addResponse =
                post(
                        "/api/inventory/"
                                + productId
                                + "/stock",
                        Map.of("quantity", 5),
                        headers);

        assertEquals(
                HttpStatus.OK,
                addResponse.getStatusCode());

        // 4. Check availability for 12 units
        ResponseEntity<Boolean> availabilityResponse =
                restTemplate.exchange(
                        getUrl(
                                "/api/inventory/"
                                        + productId
                                        + "/availability?quantity=12"),
                        HttpMethod.GET,
                        new HttpEntity<>(headers),
                        Boolean.class);

        assertEquals(
                HttpStatus.OK,
                availabilityResponse.getStatusCode());

        assertTrue(
                Boolean.TRUE.equals(
                        availabilityResponse.getBody()));

        // 5. Reserve 8 units
        ResponseEntity<String> reserveResponse =
                post(
                        "/api/inventory/"
                                + productId
                                + "/reserve",
                        Map.of("quantity", 8),
                        headers);

        assertEquals(
                HttpStatus.OK,
                reserveResponse.getStatusCode());

        // 6. Check availability for 8 units
        ResponseEntity<Boolean> afterReserveAvailability =
                restTemplate.exchange(
                        getUrl(
                                "/api/inventory/"
                                        + productId
                                        + "/availability?quantity=8"),
                        HttpMethod.GET,
                        new HttpEntity<>(headers),
                        Boolean.class);

        assertEquals(
                HttpStatus.OK,
                afterReserveAvailability.getStatusCode());

        assertFalse(
                Boolean.TRUE.equals(
                        afterReserveAvailability.getBody()));

        // 7. Release 3 reserved units
        ResponseEntity<String> releaseResponse =
                post(
                        "/api/inventory/"
                                + productId
                                + "/release",
                        Map.of("quantity", 3),
                        headers);

        assertEquals(
                HttpStatus.OK,
                releaseResponse.getStatusCode());

        // 8. Commit 5 reserved units
        ResponseEntity<String> commitResponse =
                post(
                        "/api/inventory/"
                                + productId
                                + "/commit",
                        Map.of("quantity", 5),
                        headers);

        assertEquals(
                HttpStatus.OK,
                commitResponse.getStatusCode());

        // 9. Verify final inventory
        ResponseEntity<String> finalResponse =
                get(
                        "/api/inventory/" + productId,
                        headers);

        assertEquals(
                HttpStatus.OK,
                finalResponse.getStatusCode());

        String responseBody =
                finalResponse.getBody();

        assertTrue(
                responseBody != null &&
                        responseBody.contains(
                                "\"availableStock\":10"));

        assertTrue(
                responseBody != null &&
                        responseBody.contains(
                                "\"reservedStock\":0"));
    }

    @Test
    void shouldReturnBadRequestForInvalidQuantity() {

        String productId =
                "INT-INVALID-" + System.currentTimeMillis();

        String token =
                jwtService.generateToken(
                        "integration-admin",
                        1L,
                        "ADMIN");

        HttpHeaders headers =
                createHeaders(token);

        ResponseEntity<String> response =
                post(
                        "/api/inventory/"
                                + productId
                                + "/stock",
                        Map.of("quantity", 0),
                        headers);

        assertEquals(
                HttpStatus.BAD_REQUEST,
                response.getStatusCode());
    }

    @Test
    void shouldReturnNotFoundForMissingInventory() {

        String token =
                jwtService.generateToken(
                        "integration-admin",
                        1L,
                        "ADMIN");

        HttpHeaders headers =
                createHeaders(token);

        ResponseEntity<String> response =
                get(
                        "/api/inventory/"
                                + "INT-MISSING-"
                                + System.currentTimeMillis(),
                        headers);

        assertEquals(
                HttpStatus.NOT_FOUND,
                response.getStatusCode());
    }

    @Test
    void shouldReturnConflictForInsufficientStock() {

        String productId =
                "INT-STOCK-" + System.currentTimeMillis();

        String token =
                jwtService.generateToken(
                        "integration-admin",
                        1L,
                        "ADMIN");

        HttpHeaders headers =
                createHeaders(token);

        ResponseEntity<String> createResponse =
                post(
                        "/api/inventory",
                        Map.of(
                                "productId", productId,
                                "availableStock", 5),
                        headers);

        assertEquals(
                HttpStatus.OK,
                createResponse.getStatusCode());

        ResponseEntity<String> response =
                post(
                        "/api/inventory/"
                                + productId
                                + "/reserve",
                        Map.of("quantity", 10),
                        headers);

        assertEquals(
                HttpStatus.CONFLICT,
                response.getStatusCode());
    }

    private HttpHeaders createHeaders(
            String token) {

        HttpHeaders headers =
                new HttpHeaders();

        headers.setBearerAuth(token);
        headers.setContentType(
                MediaType.APPLICATION_JSON);

        return headers;
    }

    private ResponseEntity<String> post(
            String path,
            Map<String, Object> body,
            HttpHeaders headers) {

        HttpEntity<Map<String, Object>> request =
                new HttpEntity<>(
                        body,
                        headers);

        return restTemplate.exchange(
                getUrl(path),
                HttpMethod.POST,
                request,
                String.class);
    }

    private ResponseEntity<String> get(
            String path,
            HttpHeaders headers) {

        HttpEntity<Void> request =
                new HttpEntity<>(headers);

        return restTemplate.exchange(
                getUrl(path),
                HttpMethod.GET,
                request,
                String.class);
    }

    private String getUrl(String path) {

        return "http://localhost:"
                + port
                + path;
    }
}