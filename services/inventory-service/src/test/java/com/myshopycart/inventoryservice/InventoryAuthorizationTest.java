package com.myshopycart.inventoryservice;

import com.myshopycart.inventoryservice.security.JwtService;
import com.myshopycart.inventoryservice.service.ProductOwnershipClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "myshopycart.inventory.internal-token=test-internal-token"
        })
class InventoryAuthorizationTest {

    private static final String INTERNAL_TOKEN =
            "test-internal-token";

    @LocalServerPort
    private int port;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private TestRestTemplate restTemplate;

    @MockBean
    private ProductOwnershipClient productOwnershipClient;

    private String newProductId(String prefix) {
        return prefix + "-" + UUID.randomUUID();
    }

    @Test
    void adminShouldAccessReservationEndpoint() {
        String productId = newProductId("AUTH-ADMIN");
        String token = createToken("ADMIN", 1L);

        ResponseEntity<String> response = sendPostRequest(
                "/api/inventory/" + productId + "/reserve",
                token,
                Map.of("quantity", 1));

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void distributorShouldBeForbiddenFromReservationEndpoint() {
        String productId = newProductId("AUTH-DISTRIBUTOR");
        String token = createToken("DISTRIBUTOR", 2L);

        ResponseEntity<String> response = sendPostRequest(
                "/api/inventory/" + productId + "/reserve",
                token,
                Map.of("quantity", 1));

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    void distributorShouldAccessInventoryReadEndpoint() {
        String productId = newProductId("AUTH-READ");

        when(productOwnershipClient.isOwnedByDistributor(
                productId, 2L))
                .thenReturn(true);

        String token = createToken("DISTRIBUTOR", 2L);

        ResponseEntity<String> response = sendGetRequest(
                "/api/inventory/" + productId,
                token);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void requestWithoutTokenShouldBeForbidden() {
        String productId = newProductId("AUTH-NO-TOKEN");

        ResponseEntity<String> response = restTemplate.getForEntity(
                getUrl("/api/inventory/" + productId),
                String.class);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    void internalServiceShouldAccessReservationEndpoint() {
        String productId = newProductId("AUTH-INTERNAL");

        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Internal-Service-Token", INTERNAL_TOKEN);
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(
                Map.of("quantity", 1),
                headers);

        ResponseEntity<String> response = restTemplate.exchange(
                getUrl("/api/inventory/" + productId + "/reserve"),
                HttpMethod.POST,
                request,
                String.class);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void invalidInternalTokenShouldNotAuthorizeReservation() {
        String productId = newProductId("AUTH-INVALID-INTERNAL");

        HttpHeaders headers = new HttpHeaders();
        headers.set(
                "X-Internal-Service-Token",
                "incorrect-internal-token");
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(
                Map.of("quantity", 1),
                headers);

        ResponseEntity<String> response = restTemplate.exchange(
                getUrl("/api/inventory/" + productId + "/reserve"),
                HttpMethod.POST,
                request,
                String.class);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    void distributorShouldAccessAvailabilityEndpoint() {
        String productId = newProductId("AUTH-AVAILABILITY");
        String token = createToken("DISTRIBUTOR", 2L);

        ResponseEntity<String> response = sendGetRequest(
                "/api/inventory/" + productId
                        + "/availability?quantity=1",
                token);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void distributorCanCreateInventoryForOwnedProduct() {
        String productId = newProductId("AUTH-OWNED-PRODUCT");

        when(productOwnershipClient.isOwnedByDistributor(
                productId, 2L))
                .thenReturn(true);

        String token = createToken("DISTRIBUTOR", 2L);

        ResponseEntity<String> response = sendPostRequest(
                "/api/inventory",
                token,
                Map.of(
                        "productId", productId,
                        "availableStock", 10));

        assertTrue(
                response.getStatusCode().is2xxSuccessful(),
                "Expected inventory creation to succeed, but received "
                        + response.getStatusCode() + ": " + response.getBody());
    }

    @Test
    void distributorCannotCreateInventoryForAnotherDistributorsProduct() {
        String productId = newProductId("AUTH-OTHER-PRODUCT");

        when(productOwnershipClient.isOwnedByDistributor(
                productId, 2L))
                .thenReturn(false);

        String token = createToken("DISTRIBUTOR", 2L);

        ResponseEntity<String> response = sendPostRequest(
                "/api/inventory",
                token,
                Map.of(
                        "productId", productId,
                        "availableStock", 10));

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    void distributorCanAddStockForOwnedProduct() {
        String productId = newProductId("AUTH-OWNED-STOCK");

        when(productOwnershipClient.isOwnedByDistributor(
                productId, 2L))
                .thenReturn(true);

        String token = createToken("DISTRIBUTOR", 2L);

        ResponseEntity<String> createResponse = sendPostRequest(
                "/api/inventory",
                token,
                Map.of(
                        "productId", productId,
                        "availableStock", 10));

        assertTrue(
                createResponse.getStatusCode().is2xxSuccessful(),
                "Inventory setup failed: "
                        + createResponse.getStatusCode() + ": "
                        + createResponse.getBody());

        ResponseEntity<String> response = sendPostRequest(
                "/api/inventory/" + productId + "/stock",
                token,
                Map.of("quantity", 5));

        assertTrue(
                response.getStatusCode().is2xxSuccessful(),
                "Expected stock addition to succeed, but received "
                        + response.getStatusCode() + ": "
                        + response.getBody());
    }

    @Test
    void distributorCannotAddStockForAnotherDistributorsProduct() {
        String productId = newProductId("AUTH-OTHER-STOCK");

        when(productOwnershipClient.isOwnedByDistributor(
                productId, 2L))
                .thenReturn(false);

        String token = createToken("DISTRIBUTOR", 2L);

        ResponseEntity<String> response = sendPostRequest(
                "/api/inventory/" + productId + "/stock",
                token,
                Map.of("quantity", 5));

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    private String createToken(String role, Long userId) {
        return jwtService.generateToken(
                "test-user",
                userId,
                role);
    }

    private ResponseEntity<String> sendGetRequest(
            String path,
            String token) {

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);

        HttpEntity<Void> request = new HttpEntity<>(headers);

        return restTemplate.exchange(
                getUrl(path),
                HttpMethod.GET,
                request,
                String.class);
    }

    private ResponseEntity<String> sendPostRequest(
            String path,
            String token,
            Map<String, Object> requestBody) {

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(
                requestBody,
                headers);

        return restTemplate.exchange(
                getUrl(path),
                HttpMethod.POST,
                request,
                String.class);
    }

    private String getUrl(String path) {
        return "http://localhost:" + port + path;
    }
}