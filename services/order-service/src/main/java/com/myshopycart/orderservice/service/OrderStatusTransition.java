package com.myshopycart.orderservice.service;

import com.myshopycart.orderservice.entity.OrderStatus;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

@Component
public class OrderStatusTransition {

    private final Map<OrderStatus, Set<OrderStatus>> allowedTransitions =
            new EnumMap<>(OrderStatus.class);

    public OrderStatusTransition() {

        allowedTransitions.put(
                OrderStatus.CREATED,
                EnumSet.of(
                        OrderStatus.CONFIRMED,
                        OrderStatus.CANCELLED
                )
        );

        allowedTransitions.put(
                OrderStatus.CONFIRMED,
                EnumSet.of(
                        OrderStatus.PROCESSING,
                        OrderStatus.CANCELLED
                )
        );

        allowedTransitions.put(
                OrderStatus.PROCESSING,
                EnumSet.of(
                        OrderStatus.SHIPPED,
                        OrderStatus.CANCELLED
                )
        );

        allowedTransitions.put(
                OrderStatus.SHIPPED,
                EnumSet.of(
                        OrderStatus.DELIVERED
                )
        );

        allowedTransitions.put(
                OrderStatus.DELIVERED,
                EnumSet.noneOf(OrderStatus.class)
        );

        allowedTransitions.put(
                OrderStatus.CANCELLED,
                EnumSet.noneOf(OrderStatus.class)
        );
    }

    public boolean isAllowed(
            OrderStatus currentStatus,
            OrderStatus newStatus) {

        Set<OrderStatus> allowedStatuses =
                allowedTransitions.get(currentStatus);

        return allowedStatuses != null
                && allowedStatuses.contains(newStatus);
    }
}