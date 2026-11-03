package com.myshopycart.orderservice.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class InventoryClient {

    private static final String INTERNAL_TOKEN_HEADER =
            "X-Internal-Service-Token";

    private final RestClient restClient;
    private final String internalToken;

    public InventoryClient(
            RestClient.Builder restClientBuilder,
            @Value("${inventory-service.base-url}")
            String inventoryServiceBaseUrl,
            @Value("${inventory-service.internal-token:}")
            String internalToken) {

        if (internalToken == null || internalToken.isBlank()) {
            throw new IllegalStateException(
                    "Inventory Service internal token is not configured. "
                            + "Set INVENTORY_SERVICE_INTERNAL_TOKEN.");
        }

        this.internalToken = internalToken;

        this.restClient = restClientBuilder
                .baseUrl(inventoryServiceBaseUrl)
                .build();
    }

    public boolean isStockAvailable(
            String productId,
            Integer quantity,
            String authorizationHeader) {

        Boolean available = restClient
                .get()
                .uri(uriBuilder ->
                        uriBuilder
                                .path(
                                        "/api/inventory/{productId}/availability")
                                .queryParam("quantity", quantity)
                                .build(productId))
                .header(
                        INTERNAL_TOKEN_HEADER,
                        internalToken)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .body(Boolean.class);

        return Boolean.TRUE.equals(available);
    }

    public InventoryResponse reserveStock(
            String productId,
            Integer quantity,
            String authorizationHeader) {

        ReserveStockRequest request =
                new ReserveStockRequest(quantity);

        return restClient
                .post()
                .uri(
                        "/api/inventory/{productId}/reserve",
                        productId)
                .header(
                        INTERNAL_TOKEN_HEADER,
                        internalToken)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .body(InventoryResponse.class);
    }

    public InventoryResponse releaseStock(
            String productId,
            Integer quantity,
            String authorizationHeader) {

        ReserveStockRequest request =
                new ReserveStockRequest(quantity);

        return restClient
                .post()
                .uri(
                        "/api/inventory/{productId}/release",
                        productId)
                .header(
                        INTERNAL_TOKEN_HEADER,
                        internalToken)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .body(InventoryResponse.class);
    }

    public InventoryResponse commitStock(
            String productId,
            Integer quantity,
            String authorizationHeader) {

        ReserveStockRequest request =
                new ReserveStockRequest(quantity);

        return restClient
                .post()
                .uri(
                        "/api/inventory/{productId}/commit",
                        productId)
                .header(
                        INTERNAL_TOKEN_HEADER,
                        internalToken)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .body(InventoryResponse.class);
    }

    public record ReserveStockRequest(
            Integer quantity) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record InventoryResponse(
            Long id,
            String productId,
            Integer availableStock,
            Integer reservedStock,
            Long version) {
    }
}