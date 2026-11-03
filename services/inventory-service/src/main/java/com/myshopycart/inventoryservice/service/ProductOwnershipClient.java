
package com.myshopycart.inventoryservice.service;

import com.myshopycart.inventoryservice.exception.InventoryNotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Component
public class ProductOwnershipClient {

    private final RestTemplate restTemplate;
    private final String productServiceBaseUrl;

    public ProductOwnershipClient(
            @Value("${myshopycart.product-service.base-url:http://localhost:8082}")
            String productServiceBaseUrl) {

        this.restTemplate = new RestTemplate();
        this.productServiceBaseUrl =
                productServiceBaseUrl.replaceAll("/+$", "");
    }

    public boolean isOwnedByDistributor(
            String productId,
            Long distributorId) {

        ProductOwnershipResponse product;

        try {
            product = restTemplate.getForObject(
                    productServiceBaseUrl + "/api/products/{productId}",
                    ProductOwnershipResponse.class,
                    productId);
        } catch (HttpClientErrorException.NotFound exception) {
            throw new InventoryNotFoundException(
                    "Product not found: " + productId);
        } catch (HttpClientErrorException exception) {
            throw exception;
        } catch (RestClientException exception) {
            throw new IllegalStateException(
                    "Unable to verify ownership with Product Service. "
                            + "Inventory modification was not permitted.",
                    exception);
        }

        if (product == null || product.getDistributorId() == null) {
            throw new IllegalStateException(
                    "Product ownership could not be verified. "
                            + "Inventory modification was not permitted.");
        }

        return String.valueOf(distributorId)
                .equals(product.getDistributorId());
    }

    public static class ProductOwnershipResponse {

        private String distributorId;

        public ProductOwnershipResponse() {
        }

        public String getDistributorId() {
            return distributorId;
        }

        public void setDistributorId(String distributorId) {
            this.distributorId = distributorId;
        }
    }
}
