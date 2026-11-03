package com.myshopycart.orderservice.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class AuthAddressClient {

    private final RestClient restClient;

    public AuthAddressClient(
            RestClient.Builder restClientBuilder,
            @Value("${auth-service.base-url}") String authServiceBaseUrl) {

        this.restClient = restClientBuilder
                .baseUrl(authServiceBaseUrl)
                .build();
    }

    public UserAddressResponse getAddress(
            Long addressId,
            String authorizationHeader) {

        return restClient
                .get()
                .uri("/api/v1/addresses/{id}", addressId)
                .header(
                        HttpHeaders.AUTHORIZATION,
                        authorizationHeader)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .body(UserAddressResponse.class);
    }

    public CustomerContactResponse getCustomerContact(
            Long userId,
            String authorizationHeader) {

        return restClient
                .get()
                .uri("/api/auth/users/{userId}/contact", userId)
                .header(
                        HttpHeaders.AUTHORIZATION,
                        authorizationHeader)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .body(CustomerContactResponse.class);
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record UserAddressResponse(
            Long id,
            Long userId,
            String addressType,
            String address,
            String city,
            String state,
            String postalCode,
            String country,
            boolean defaultAddress) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record CustomerContactResponse(
            Long userId,
            String username,
            String email,
            String mobileNumber) {
    }
}