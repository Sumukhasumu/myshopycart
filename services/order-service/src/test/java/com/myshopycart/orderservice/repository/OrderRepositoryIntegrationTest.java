package com.myshopycart.orderservice.repository;

import com.myshopycart.orderservice.entity.Order;
import com.myshopycart.orderservice.entity.OrderItem;
import com.myshopycart.orderservice.entity.OrderStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(properties = "minio.initialize-bucket=false")
@ActiveProfiles("test")
@Testcontainers
class OrderRepositoryIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:15-alpine")
                    .withDatabaseName("myshopycart_order_test")
                    .withUsername("test")
                    .withPassword("test");

    @DynamicPropertySource
    static void configureDatasource(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.datasource.driver-class-name", postgres::getDriverClassName);
    }

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Test
    void shouldPersistAndFindOrder() {
        Instant now = Instant.now();

        Order order = Order.builder()
                .userId(25L)
                .totalAmount(BigDecimal.valueOf(2000))
                .status(OrderStatus.CREATED)
                .createdAt(now)
                .updatedAt(now)
                .build();

        Order savedOrder = orderRepository.save(order);

        assertNotNull(savedOrder.getId());

        Optional<Order> foundOrder =
                orderRepository.findById(savedOrder.getId());

        assertEquals(
                savedOrder.getId(),
                foundOrder.orElseThrow().getId()
        );

        assertEquals(
                25L,
                foundOrder.orElseThrow().getUserId()
        );

        assertEquals(
                0,
                BigDecimal.valueOf(2000)
                        .compareTo(foundOrder.orElseThrow().getTotalAmount())
        );

        assertEquals(
                OrderStatus.CREATED,
                foundOrder.orElseThrow().getStatus()
        );
    }

    @Test
    void shouldFindOrdersByUserIdOrderedByCreatedAtDescending() {
        Instant olderTime =
                Instant.parse("2026-01-01T10:00:00Z");

        Instant newerTime =
                Instant.parse("2026-01-02T10:00:00Z");

        Order olderOrder = Order.builder()
                .userId(100L)
                .totalAmount(BigDecimal.valueOf(1000))
                .status(OrderStatus.CREATED)
                .createdAt(olderTime)
                .updatedAt(olderTime)
                .build();

        Order newerOrder = Order.builder()
                .userId(100L)
                .totalAmount(BigDecimal.valueOf(2000))
                .status(OrderStatus.CONFIRMED)
                .createdAt(newerTime)
                .updatedAt(newerTime)
                .build();

        orderRepository.save(olderOrder);
        orderRepository.save(newerOrder);

        List<Order> orders =
                orderRepository.findByUserIdOrderByCreatedAtDesc(100L);

        assertEquals(2, orders.size());

        assertEquals(
                newerOrder.getId(),
                orders.get(0).getId()
        );

        assertEquals(
                olderOrder.getId(),
                orders.get(1).getId()
        );
    }

    @Test
    void shouldPersistOrderWithOrderItems() {
        Instant now = Instant.now();

        Order order = Order.builder()
                .userId(200L)
                .totalAmount(BigDecimal.valueOf(3000))
                .status(OrderStatus.CREATED)
                .createdAt(now)
                .updatedAt(now)
                .build();

        OrderItem firstItem = OrderItem.builder()
                .order(order)
                .productId("product-1")
                .productName("Product One")
                .quantity(2)
                .price(BigDecimal.valueOf(1000))
                .createdAt(now)
                .build();

        OrderItem secondItem = OrderItem.builder()
                .order(order)
                .productId("product-2")
                .productName("Product Two")
                .quantity(1)
                .price(BigDecimal.valueOf(1000))
                .createdAt(now)
                .build();

        order.getItems().add(firstItem);
        order.getItems().add(secondItem);

        Order savedOrder = orderRepository.save(order);

        assertNotNull(savedOrder.getId());

        List<OrderItem> savedItems =
                orderItemRepository.findByOrderId(savedOrder.getId());

        assertEquals(2, savedItems.size());

        assertEquals(
                "product-1",
                savedItems.get(0).getProductId()
        );

        assertEquals(
                "Product One",
                savedItems.get(0).getProductName()
        );

        assertEquals(
                "product-2",
                savedItems.get(1).getProductId()
        );

        assertEquals(
                "Product Two",
                savedItems.get(1).getProductName()
        );

        assertEquals(
                2,
                savedItems.get(0).getQuantity()
        );

        assertEquals(
                1,
                savedItems.get(1).getQuantity()
        );

        assertEquals(
                savedOrder.getId(),
                savedItems.get(0).getOrder().getId()
        );

        assertEquals(
                savedOrder.getId(),
                savedItems.get(1).getOrder().getId()
        );
    }

    @Test
    void shouldPersistUpdatedOrderStatus() {
        Instant createdTime = Instant.parse("2026-09-23T04:00:00Z");

        Order order = Order.builder()
                .userId(300L)
                .totalAmount(BigDecimal.valueOf(1500))
                .status(OrderStatus.CREATED)
                .createdAt(createdTime)
                .updatedAt(createdTime)
                .build();

        Order savedOrder = orderRepository.save(order);

        assertEquals(
                OrderStatus.CREATED,
                savedOrder.getStatus()
        );

        Instant updatedTime = Instant.parse("2026-09-23T04:05:00Z");

        savedOrder.setStatus(OrderStatus.CONFIRMED);
        savedOrder.setUpdatedAt(updatedTime);

        orderRepository.save(savedOrder);

        Order updatedOrder =
                orderRepository.findById(savedOrder.getId())
                        .orElseThrow();

        assertEquals(
                OrderStatus.CONFIRMED,
                updatedOrder.getStatus()
        );

        assertEquals(
                updatedTime,
                updatedOrder.getUpdatedAt()
        );
    }
}