package com.myshopycart.orderservice.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;

@Component
public class CartClient {

    private final RestClient restClient;

    public CartClient(
            RestClient.Builder restClientBuilder,
            @Value("${cart-service.base-url}") String cartServiceBaseUrl) {

        this.restClient = restClientBuilder
                .baseUrl(cartServiceBaseUrl)
                .build();
    }

    public CartResponse getCart(String authorizationHeader) {

        return restClient
                .get()
                .uri("/api/carts")
                .header(
                        HttpHeaders.AUTHORIZATION,
                        authorizationHeader)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .body(CartResponse.class);
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record CartResponse(
            Long cartId,
            Long userId,
            List<CartItemResponse> items) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record CartItemResponse(
            Long id,
            Long cartId,
            String productId,
            Integer quantity) {
    }
}