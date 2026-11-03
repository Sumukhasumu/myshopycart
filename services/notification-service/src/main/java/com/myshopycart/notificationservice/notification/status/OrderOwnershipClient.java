
package com.myshopycart.notificationservice.notification.status;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Component
public class OrderOwnershipClient {

    private final RestTemplate restTemplate;
    private final String orderServiceBaseUrl;

    public OrderOwnershipClient(
            @Value("${myshopycart.order-service.base-url:http://localhost:8085}")
            String orderServiceBaseUrl) {

        this.restTemplate = new RestTemplate();
        this.orderServiceBaseUrl =
                orderServiceBaseUrl.replaceAll("/+$", "");
    }

    public boolean isOrderAccessible(
            Long orderId,
            String authorizationHeader) {

        if (orderId == null
                || authorizationHeader == null
                || !authorizationHeader.startsWith("Bearer ")) {
            return false;
        }

        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.AUTHORIZATION, authorizationHeader);

        HttpEntity<Void> request = new HttpEntity<>(headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    orderServiceBaseUrl + "/api/orders/{orderId}",
                    HttpMethod.GET,
                    request,
                    String.class,
                    orderId);

            return response.getStatusCode().is2xxSuccessful();

        } catch (HttpStatusCodeException exception) {
            if (exception.getStatusCode() == HttpStatus.NOT_FOUND
                    || exception.getStatusCode() == HttpStatus.FORBIDDEN
                    || exception.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                return false;
            }

            throw new IllegalStateException(
                    "Unable to verify order ownership. "
                            + "Notification status access was denied.",
                    exception);

        } catch (RestClientException exception) {
            throw new IllegalStateException(
                    "Order Service is unavailable. "
                            + "Notification status access was denied.",
                    exception);
        }
    }
}