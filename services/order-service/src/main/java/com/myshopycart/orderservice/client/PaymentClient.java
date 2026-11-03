
package com.myshopycart.orderservice.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Component
public class PaymentClient {

    private final RestClient restClient;

    public PaymentClient(
            RestClient.Builder restClientBuilder,
            @Value("${payment-service.base-url}")
            String paymentServiceBaseUrl) {

        this.restClient = restClientBuilder
                .baseUrl(paymentServiceBaseUrl)
                .build();
    }

    public PaymentResponse processPayment(
            Long orderId,
            Long userId,
            BigDecimal amount,
            String idempotencyKey,
            String paymentMethod,
            String simulationMode,
            String authorizationHeader) {

        PaymentRequest request = new PaymentRequest(
                orderId,
                userId,
                amount,
                idempotencyKey,
                paymentMethod,
                simulationMode);

        return restClient
                .post()
                .uri("/api/payments")
                .headers(headers -> {
                    if (authorizationHeader != null
                            && !authorizationHeader.isBlank()) {
                        headers.set(
                                HttpHeaders.AUTHORIZATION,
                                authorizationHeader);
                    }
                })
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .body(PaymentResponse.class);
    }

    /**
     * Backward-compatible overload for older tests and callers.
     * New checkout code must use the full method above.
     */
    @Deprecated
    public PaymentResponse processPayment(
            Long orderId,
            Long userId,
            BigDecimal amount,
            String idempotencyKey,
            String authorizationHeader) {

        return processPayment(
                orderId,
                userId,
                amount,
                idempotencyKey,
                "UPI",
                "SUCCESS",
                authorizationHeader);
    }

    public record PaymentRequest(
            Long orderId,
            Long userId,
            BigDecimal amount,
            String idempotencyKey,
            String paymentMethod,
            String simulationMode) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record PaymentResponse(
            Long id,
            Long orderId,
            Long userId,
            BigDecimal amount,
            String status,
            String idempotencyKey,
            String paymentMethod,
            String gatewayReference,
            String failureReason,
            LocalDateTime createdAt,
            LocalDateTime updatedAt) {

        /**
         * Backward-compatible constructor for existing tests.
         */
        @Deprecated
        public PaymentResponse(
                Long id,
                Long orderId,
                Long userId,
                BigDecimal amount,
                String status,
                String idempotencyKey,
                String gatewayReference,
                String failureReason,
                LocalDateTime createdAt,
                LocalDateTime updatedAt) {

            this(
                    id,
                    orderId,
                    userId,
                    amount,
                    status,
                    idempotencyKey,
                    null,
                    gatewayReference,
                    failureReason,
                    createdAt,
                    updatedAt);
        }
    }
}