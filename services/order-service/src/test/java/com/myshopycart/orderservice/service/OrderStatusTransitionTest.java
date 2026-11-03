package com.myshopycart.orderservice.service;

import com.myshopycart.orderservice.entity.OrderStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OrderStatusTransitionTest {

    private OrderStatusTransition orderStatusTransition;

    @BeforeEach
    void setUp() {
        orderStatusTransition = new OrderStatusTransition();
    }

    @Test
    void shouldAllowCreatedToConfirmed() {

        assertTrue(
                orderStatusTransition.isAllowed(
                        OrderStatus.CREATED,
                        OrderStatus.CONFIRMED));
    }

    @Test
    void shouldAllowCreatedToCancelled() {

        assertTrue(
                orderStatusTransition.isAllowed(
                        OrderStatus.CREATED,
                        OrderStatus.CANCELLED));
    }

    @Test
    void shouldAllowConfirmedToProcessing() {

        assertTrue(
                orderStatusTransition.isAllowed(
                        OrderStatus.CONFIRMED,
                        OrderStatus.PROCESSING));
    }

    @Test
    void shouldAllowProcessingToShipped() {

        assertTrue(
                orderStatusTransition.isAllowed(
                        OrderStatus.PROCESSING,
                        OrderStatus.SHIPPED));
    }

    @Test
    void shouldAllowShippedToDelivered() {

        assertTrue(
                orderStatusTransition.isAllowed(
                        OrderStatus.SHIPPED,
                        OrderStatus.DELIVERED));
    }

    @Test
    void shouldRejectCreatedToShipped() {

        assertFalse(
                orderStatusTransition.isAllowed(
                        OrderStatus.CREATED,
                        OrderStatus.SHIPPED));
    }

    @Test
    void shouldRejectDeliveredToCancelled() {

        assertFalse(
                orderStatusTransition.isAllowed(
                        OrderStatus.DELIVERED,
                        OrderStatus.CANCELLED));
    }

    @Test
    void shouldRejectCancelledToConfirmed() {

        assertFalse(
                orderStatusTransition.isAllowed(
                        OrderStatus.CANCELLED,
                        OrderStatus.CONFIRMED));
    }

    @Test
    void shouldRejectShippedToCreated() {

        assertFalse(
                orderStatusTransition.isAllowed(
                        OrderStatus.SHIPPED,
                        OrderStatus.CREATED));
    }

    @Test
    void shouldAllowCompleteOrderLifecycle() {

        OrderStatus currentStatus = OrderStatus.CREATED;

        assertTrue(
                orderStatusTransition.isAllowed(
                        currentStatus,
                        OrderStatus.CONFIRMED));

        currentStatus = OrderStatus.CONFIRMED;

        assertTrue(
                orderStatusTransition.isAllowed(
                        currentStatus,
                        OrderStatus.PROCESSING));

        currentStatus = OrderStatus.PROCESSING;

        assertTrue(
                orderStatusTransition.isAllowed(
                        currentStatus,
                        OrderStatus.SHIPPED));

        currentStatus = OrderStatus.SHIPPED;

        assertTrue(
                orderStatusTransition.isAllowed(
                        currentStatus,
                        OrderStatus.DELIVERED));
    }

    @Test
    void shouldAllowCancellationBeforeShipping() {

        assertTrue(
                orderStatusTransition.isAllowed(
                        OrderStatus.CREATED,
                        OrderStatus.CANCELLED));

        assertTrue(
                orderStatusTransition.isAllowed(
                        OrderStatus.CONFIRMED,
                        OrderStatus.CANCELLED));

        assertTrue(
                orderStatusTransition.isAllowed(
                        OrderStatus.PROCESSING,
                        OrderStatus.CANCELLED));
    }

    @Test
    void shouldNotAllowTransitionFromFinalStatuses() {

        assertFalse(
                orderStatusTransition.isAllowed(
                        OrderStatus.DELIVERED,
                        OrderStatus.CONFIRMED));

        assertFalse(
                orderStatusTransition.isAllowed(
                        OrderStatus.CANCELLED,
                        OrderStatus.CONFIRMED));
    }
}