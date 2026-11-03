package com.myshopycart.orderservice.controller;

import com.myshopycart.orderservice.client.CartClient;
import com.myshopycart.orderservice.dto.CreateOrderRequest;
import com.myshopycart.orderservice.dto.OrderItemRequest;
import com.myshopycart.orderservice.dto.UpdateOrderStatusRequest;
import com.myshopycart.orderservice.entity.Order;
import com.myshopycart.orderservice.entity.OrderStatus;
import com.myshopycart.orderservice.service.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import com.myshopycart.orderservice.dto.CheckoutRequest;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class OrderControllerTest {

    private OrderService orderService;

    private OrderController orderController;

    private Authentication authentication;

    @BeforeEach
    void setUp() {

        orderService =
                mock(OrderService.class);

        authentication =
                mock(Authentication.class);

        orderController =
                new OrderController(orderService);
    }

    @Test
    void shouldRequireAdminRoleForStatusUpdate()
            throws Exception {

        Method method =
                OrderController.class.getMethod(
                        "updateStatus",
                        Long.class,
                        UpdateOrderStatusRequest.class);

        PreAuthorize preAuthorize =
                method.getAnnotation(
                        PreAuthorize.class);

        assertNotNull(preAuthorize);

        assertEquals(
                "hasRole('ADMIN')",
                preAuthorize.value());
    }

    @Test
    void shouldCreateOrderUsingAuthenticatedUserId()
            throws Exception {

        CreateOrderRequest request =
                new CreateOrderRequest(
                        List.of(
                                new OrderItemRequest(
                                        "PROD-001",
                                        2,
                                        BigDecimal.valueOf(500))));

        Order order =
                Order.builder()
                        .id(1L)
                        .userId(25L)
                        .status(OrderStatus.CREATED)
                        .createdAt(Instant.now())
                        .updatedAt(Instant.now())
                        .build();

        when(authentication.getDetails())
                .thenReturn(25L);

        when(orderService.createOrder(
                request,
                25L,
                "Bearer token"))
                .thenReturn(order);

        ResponseEntity<Order> response =
                orderController.createOrder(
                        request,
                        "Bearer token",
                        authentication);

        assertEquals(
                201,
                response.getStatusCode().value());

        assertEquals(
                order,
                response.getBody());

        verify(orderService)
                .createOrder(
                        request,
                        25L,
                        "Bearer token");
    }

    @Test
    void shouldGetOrderHistoryUsingAuthenticatedUserId() {

        Order order =
                Order.builder()
                        .id(1L)
                        .userId(25L)
                        .status(OrderStatus.CREATED)
                        .build();

        when(authentication.getDetails())
                .thenReturn(25L);

        when(orderService.findByUserId(25L))
                .thenReturn(List.of(order));

        ResponseEntity<List<Order>> response =
                orderController.getOrderHistory(
                        authentication);

        assertEquals(
                200,
                response.getStatusCode().value());

        assertEquals(
                1,
                response.getBody().size());

        assertEquals(
                order,
                response.getBody().get(0));

        verify(orderService)
                .findByUserId(25L);
    }

    @Test
    void shouldGetOrderByIdUsingAuthenticatedUserId() {

        Order order =
                Order.builder()
                        .id(10L)
                        .userId(25L)
                        .status(OrderStatus.CREATED)
                        .build();

        when(authentication.getDetails())
                .thenReturn(25L);

        when(orderService.findByIdForUser(
                10L,
                25L))
                .thenReturn(
                        java.util.Optional.of(order));

        ResponseEntity<Order> response =
                orderController.getOrderById(
                        10L,
                        authentication);

        assertEquals(
                200,
                response.getStatusCode().value());

        assertEquals(
                order,
                response.getBody());

        verify(orderService)
                .findByIdForUser(
                        10L,
                        25L);
    }

    @Test
    void shouldReturnNotFoundWhenOrderDoesNotBelongToUser() {

        when(authentication.getDetails())
                .thenReturn(25L);

        when(orderService.findByIdForUser(
                10L,
                25L))
                .thenReturn(
                        java.util.Optional.empty());

        ResponseEntity<Order> response =
                orderController.getOrderById(
                        10L,
                        authentication);

        assertEquals(
                404,
                response.getStatusCode().value());

        assertEquals(
                null,
                response.getBody());

        verify(orderService)
                .findByIdForUser(
                        10L,
                        25L);
    }

    @Test
    void shouldGetCheckoutCartUsingAuthorizationHeader() {

        CartClient.CartResponse cartResponse =
                new CartClient.CartResponse(
                        1L,
                        25L,
                        List.of());

        when(orderService.getCart(
                "Bearer token"))
                .thenReturn(cartResponse);

        ResponseEntity<CartClient.CartResponse> response =
                orderController.getCheckoutCart(
                        "Bearer token");

        assertEquals(
                200,
                response.getStatusCode().value());

        assertEquals(
                cartResponse,
                response.getBody());

        verify(orderService)
                .getCart("Bearer token");
    }

    @Test
    void shouldCreateOrderFromCartUsingAuthenticatedUserId() {

        CheckoutRequest request = new CheckoutRequest(
                "Test Customer",
                "9876543210",
                "123 Main Street",
                "Bengaluru",
                "Karnataka",
                "560001"
        );

        Order order = Order.builder()
                .id(20L)
                .userId(25L)
                .status(OrderStatus.CREATED)
                .build();

        when(authentication.getDetails())
                .thenReturn(25L);

        when(orderService.createOrderFromCart(
                request,
                "Bearer token",
                25L))
                .thenReturn(order);

        ResponseEntity<Order> response =
                orderController.createOrderFromCart(
                        request,
                        "Bearer token",
                        authentication);

        assertEquals(
                201,
                response.getStatusCode().value());

        assertEquals(
                order,
                response.getBody());

        verify(orderService)
                .createOrderFromCart(
                        request,
                        "Bearer token",
                        25L);
    }

    @Test
    void shouldUpdateOrderStatus() {

        Order order =
                Order.builder()
                        .id(10L)
                        .userId(25L)
                        .status(OrderStatus.CONFIRMED)
                        .build();

        UpdateOrderStatusRequest request =
                new UpdateOrderStatusRequest(
                        OrderStatus.CONFIRMED);

        when(orderService.updateStatus(
                10L,
                OrderStatus.CONFIRMED))
                .thenReturn(order);

        ResponseEntity<Order> response =
                orderController.updateStatus(
                        10L,
                        request);

        assertEquals(
                200,
                response.getStatusCode().value());

        assertEquals(
                order,
                response.getBody());

        verify(orderService)
                .updateStatus(
                        10L,
                        OrderStatus.CONFIRMED);
    }

    @Test
    void shouldRejectMissingAuthentication() {

        IllegalStateException exception =
                assertThrows(
                        IllegalStateException.class,
                        () -> orderController.getOrderHistory(
                                null));

        assertEquals(
                "Authenticated user ID not available",
                exception.getMessage());
    }

    @Test
    void shouldRejectMissingAuthenticatedUserId() {

        when(authentication.getDetails())
                .thenReturn(null);

        IllegalStateException exception =
                assertThrows(
                        IllegalStateException.class,
                        () -> orderController.getOrderHistory(
                                authentication));

        assertEquals(
                "Authenticated user ID not available",
                exception.getMessage());
    }
    
    @Test
void shouldRejectInvalidAuthenticatedUserIdType() {

    when(authentication.getDetails())
            .thenReturn("invalid-user-id");

    assertThrows(
            ClassCastException.class,
            () -> orderController.getOrderHistory(
                    authentication));
    }
}