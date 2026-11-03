package com.myshopycart.orderservice.client;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.queryParam;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class InventoryClientTest {

    private static final String BASE_URL =
            "http://localhost:8083";

    private static final String INTERNAL_TOKEN =
            "test-internal-token";

    private InventoryClient createClient(
            RestClient.Builder builder) {

        return new InventoryClient(
                builder,
                BASE_URL,
                INTERNAL_TOKEN);
    }

    @Test
    void shouldCheckStockAvailability() {

        RestClient.Builder builder = RestClient.builder();

        MockRestServiceServer server =
                MockRestServiceServer.bindTo(builder).build();

        InventoryClient client = createClient(builder);

        server.expect(requestTo(
                        BASE_URL
                                + "/api/inventory/product-test-1/availability?quantity=2"))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header(
                        "X-Internal-Service-Token",
                        INTERNAL_TOKEN))
                .andExpect(queryParam("quantity", "2"))
                .andRespond(withSuccess(
                        "true",
                        MediaType.APPLICATION_JSON));

        boolean available = client.isStockAvailable(
                "product-test-1",
                2,
                "Bearer customer-token");

        assertTrue(available);
        server.verify();
    }

    @Test
    void shouldReturnFalseWhenStockIsUnavailable() {

        RestClient.Builder builder = RestClient.builder();

        MockRestServiceServer server =
                MockRestServiceServer.bindTo(builder).build();

        InventoryClient client = createClient(builder);

        server.expect(requestTo(
                        BASE_URL
                                + "/api/inventory/product-test-1/availability?quantity=20"))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header(
                        "X-Internal-Service-Token",
                        INTERNAL_TOKEN))
                .andExpect(queryParam("quantity", "20"))
                .andRespond(withSuccess(
                        "false",
                        MediaType.APPLICATION_JSON));

        boolean available = client.isStockAvailable(
                "product-test-1",
                20,
                "Bearer customer-token");

        assertEquals(false, available);
        server.verify();
    }

    @Test
    void shouldReserveStock() {

        RestClient.Builder builder = RestClient.builder();

        MockRestServiceServer server =
                MockRestServiceServer.bindTo(builder).build();

        InventoryClient client = createClient(builder);

        server.expect(requestTo(
                        BASE_URL
                                + "/api/inventory/product-test-1/reserve"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header(
                        "X-Internal-Service-Token",
                        INTERNAL_TOKEN))
                .andExpect(content().contentType(
                        MediaType.APPLICATION_JSON))
                .andExpect(content().json("{\"quantity\":2}"))
                .andRespond(withSuccess(
                        """
                        {
                            "id": 22,
                            "productId": "product-test-1",
                            "availableStock": 8,
                            "reservedStock": 2,
                            "version": 1
                        }
                        """,
                        MediaType.APPLICATION_JSON));

        InventoryClient.InventoryResponse response =
                client.reserveStock(
                        "product-test-1",
                        2,
                        "Bearer customer-token");

        assertNotNull(response);
        assertEquals(22L, response.id());
        assertEquals("product-test-1", response.productId());
        assertEquals(8, response.availableStock());
        assertEquals(2, response.reservedStock());
        assertEquals(1L, response.version());

        server.verify();
    }

    @Test
    void shouldMapDifferentReserveStockResponse() {

        RestClient.Builder builder = RestClient.builder();

        MockRestServiceServer server =
                MockRestServiceServer.bindTo(builder).build();

        InventoryClient client = createClient(builder);

        server.expect(requestTo(
                        BASE_URL
                                + "/api/inventory/product-test-2/reserve"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header(
                        "X-Internal-Service-Token",
                        INTERNAL_TOKEN))
                .andExpect(content().contentType(
                        MediaType.APPLICATION_JSON))
                .andExpect(content().json("{\"quantity\":3}"))
                .andRespond(withSuccess(
                        """
                        {
                            "id": 30,
                            "productId": "product-test-2",
                            "availableStock": 17,
                            "reservedStock": 3,
                            "version": 4
                        }
                        """,
                        MediaType.APPLICATION_JSON));

        InventoryClient.InventoryResponse response =
                client.reserveStock(
                        "product-test-2",
                        3,
                        "Bearer customer-token");

        assertNotNull(response);
        assertEquals(30L, response.id());
        assertEquals("product-test-2", response.productId());
        assertEquals(17, response.availableStock());
        assertEquals(3, response.reservedStock());
        assertEquals(4L, response.version());

        server.verify();
    }

    @Test
    void shouldReleaseStockUsingInternalToken() {

        RestClient.Builder builder = RestClient.builder();

        MockRestServiceServer server =
                MockRestServiceServer.bindTo(builder).build();

        InventoryClient client = createClient(builder);

        server.expect(requestTo(
                        BASE_URL
                                + "/api/inventory/product-test-1/release"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header(
                        "X-Internal-Service-Token",
                        INTERNAL_TOKEN))
                .andExpect(content().json("{\"quantity\":2}"))
                .andRespond(withSuccess(
                        """
                        {
                            "id": 22,
                            "productId": "product-test-1",
                            "availableStock": 10,
                            "reservedStock": 0,
                            "version": 2
                        }
                        """,
                        MediaType.APPLICATION_JSON));

        InventoryClient.InventoryResponse response =
                client.releaseStock(
                        "product-test-1",
                        2,
                        "Bearer customer-token");

        assertNotNull(response);
        assertEquals(10, response.availableStock());
        assertEquals(0, response.reservedStock());

        server.verify();
    }

    @Test
    void shouldCommitStockUsingInternalToken() {

        RestClient.Builder builder = RestClient.builder();

        MockRestServiceServer server =
                MockRestServiceServer.bindTo(builder).build();

        InventoryClient client = createClient(builder);

        server.expect(requestTo(
                        BASE_URL
                                + "/api/inventory/product-test-1/commit"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header(
                        "X-Internal-Service-Token",
                        INTERNAL_TOKEN))
                .andExpect(content().json("{\"quantity\":2}"))
                .andRespond(withSuccess(
                        """
                        {
                            "id": 22,
                            "productId": "product-test-1",
                            "availableStock": 8,
                            "reservedStock": 0,
                            "version": 3
                        }
                        """,
                        MediaType.APPLICATION_JSON));

        InventoryClient.InventoryResponse response =
                client.commitStock(
                        "product-test-1",
                        2,
                        "Bearer customer-token");

        assertNotNull(response);
        assertEquals(8, response.availableStock());
        assertEquals(0, response.reservedStock());

        server.verify();
    }

    @Test
    void shouldRejectMissingInternalTokenConfiguration() {

        RestClient.Builder builder = RestClient.builder();

        assertThrows(
                IllegalStateException.class,
                () -> new InventoryClient(
                        builder,
                        BASE_URL,
                        " "));
    }
}