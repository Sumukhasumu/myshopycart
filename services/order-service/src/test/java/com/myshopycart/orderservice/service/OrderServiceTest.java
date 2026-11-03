package com.myshopycart.orderservice.service;

import com.myshopycart.orderservice.client.AuthAddressClient;
import com.myshopycart.orderservice.client.CartClient;
import com.myshopycart.orderservice.client.InventoryClient;
import com.myshopycart.orderservice.client.PaymentClient;
import com.myshopycart.orderservice.client.ProductClient;
import com.myshopycart.orderservice.dto.CreateOrderRequest;
import com.myshopycart.orderservice.dto.OrderItemRequest;
import com.myshopycart.orderservice.entity.Order;
import com.myshopycart.orderservice.entity.OrderItem;
import com.myshopycart.orderservice.entity.OrderStatus;
import com.myshopycart.orderservice.repository.OrderRepository;
import com.myshopycart.orderservice.messaging.event.MyShopyCartEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.times;
import com.myshopycart.orderservice.messaging.producer.OrderEventProducer;
import org.mockito.ArgumentCaptor;
import com.myshopycart.orderservice.messaging.event.OrderConfirmedPayload;
import static org.junit.jupiter.api.Assertions.assertNull;

class OrderServiceTest {

    private OrderRepository orderRepository;
    private OrderStatusTransition orderStatusTransition;
    private CartClient cartClient;
    private ProductClient productClient;
    private InventoryClient inventoryClient;
    private PaymentClient paymentClient;
    private AuthAddressClient authAddressClient;
    private OrderEventProducer orderEventProducer;
    private InvoiceService invoiceService;
    private InvoicePdfGenerator invoicePdfGenerator;
    private MinioStorageService minioStorageService;

    private OrderService orderService;

    @BeforeEach
    void setUp() {

        orderRepository =
                org.mockito.Mockito.mock(OrderRepository.class);

        orderStatusTransition =
                org.mockito.Mockito.mock(
                        OrderStatusTransition.class);

        cartClient =
                org.mockito.Mockito.mock(CartClient.class);

        productClient =
                org.mockito.Mockito.mock(ProductClient.class);

        inventoryClient =
                org.mockito.Mockito.mock(
                        InventoryClient.class);

        paymentClient =
                org.mockito.Mockito.mock(
                        PaymentClient.class);

        authAddressClient =
                org.mockito.Mockito.mock(
                        AuthAddressClient.class);

	orderEventProducer =
        	org.mockito.Mockito.mock(
                	OrderEventProducer.class);
	invoiceService =
        org.mockito.Mockito.mock(
                InvoiceService.class);

	invoicePdfGenerator =
        org.mockito.Mockito.mock(
                InvoicePdfGenerator.class);

	minioStorageService =
        org.mockito.Mockito.mock(
                MinioStorageService.class);

	com.myshopycart.orderservice.entity.Invoice invoice =
        	org.mockito.Mockito.mock(
                	com.myshopycart.orderservice.entity.Invoice.class);

	when(invoice.getInvoiceNumber())
        	.thenReturn("INV-TEST-000001");

	when(invoiceService.createInvoice(
        	any(Order.class),
        	any(InvoiceCustomerSnapshot.class),
        	any(String.class)))
        	.thenReturn(invoice);

	when(invoicePdfGenerator.generate(
        	any(com.myshopycart.orderservice.entity.Invoice.class)))
        	.thenReturn("TEST-INVOICE-PDF".getBytes());

        when(authAddressClient.getAddress(
                any(Long.class),
                any(String.class)))
                .thenAnswer(invocation -> {
                    Long addressId = invocation.getArgument(0);

                    return new AuthAddressClient.UserAddressResponse(
                            addressId,
                            25L,
                            "SHIPPING",
                            "MyShopyCart Address",
                            "Udupi",
                            "Karnataka",
                            "576101",
                            "India",
                            true);
                });

        when(authAddressClient.getCustomerContact(
                any(Long.class),
                any(String.class)))
                .thenReturn(
                        new AuthAddressClient.CustomerContactResponse(
                                25L,
                                "testuser",
                                "testuser@example.com",
                                "9876543210"));

        orderService = new OrderService(
        	orderRepository,
        	orderStatusTransition,
        	cartClient,
        	productClient,
        	inventoryClient,
        	paymentClient,
        	orderEventProducer,
        	authAddressClient,
        	invoiceService,
			invoicePdfGenerator,
			minioStorageService);
    }

    @Test
    void shouldCreateOrderWithItemsAndTotalUsingProductPrice() {

        OrderItemRequest item1 =
                new OrderItemRequest(
                        "PROD-001",
                        2,
                        BigDecimal.valueOf(1));

        OrderItemRequest item2 =
                new OrderItemRequest(
                        "PROD-002",
                        1,
                        BigDecimal.valueOf(1));

        CreateOrderRequest request =
                new CreateOrderRequest(
                        List.of(item1, item2),
                        1L,
                        2L);

        when(productClient.getProduct(
                "PROD-001",
                "Bearer token"))
                .thenReturn(
                        new ProductClient.ProductResponse(
                                "PROD-001",
                                "Product 1",
                                BigDecimal.valueOf(500)));

        when(productClient.getProduct(
                "PROD-002",
                "Bearer token"))
                .thenReturn(
                        new ProductClient.ProductResponse(
                                "PROD-002",
                                "Product 2",
                                BigDecimal.valueOf(1000)));

        when(orderRepository.save(any(Order.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0));

        Order order =
                orderService.createOrder(
                        request,
                        25L,
                        "Bearer token");

        assertNotNull(order);

        assertEquals(
                25L,
                order.getUserId());

        assertEquals(
                OrderStatus.CREATED,
                order.getStatus());

        assertEquals(
                BigDecimal.valueOf(2000),
                order.getTotalAmount());

        assertEquals(
                2,
                order.getItems().size());

        assertEquals(
                BigDecimal.valueOf(500),
                order.getItems()
                        .get(0)
                        .getPrice());

        assertEquals(
                BigDecimal.valueOf(1000),
                order.getItems()
                        .get(1)
                        .getPrice());

        verify(orderRepository)
                .save(any(Order.class));
    }

    @Test
    void shouldRejectZeroQuantity() {

        OrderItemRequest item =
                new OrderItemRequest(
                        "PROD-001",
                        0,
                        BigDecimal.valueOf(500));

        CreateOrderRequest request =
                new CreateOrderRequest(
                        List.of(item),
                        1L,
                        2L);

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> orderService.createOrder(
                                request,
                                25L,
                                "Bearer token"));

        assertEquals(
                "Order quantity must be greater than zero",
                exception.getMessage());

        verify(productClient, never())
                .getProduct(
                        "PROD-001",
                        "Bearer token");

        verify(orderRepository, never())
                .save(any(Order.class));
    }

    @Test
    void shouldRejectNegativeQuantity() {

        OrderItemRequest item =
                new OrderItemRequest(
                        "PROD-001",
                        -1,
                        BigDecimal.valueOf(500));

        CreateOrderRequest request =
                new CreateOrderRequest(
                        List.of(item),
                        1L,
                        2L);

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> orderService.createOrder(
                                request,
                                25L,
                                "Bearer token"));

        assertEquals(
                "Order quantity must be greater than zero",
                exception.getMessage());

        verify(productClient, never())
                .getProduct(
                        "PROD-001",
                        "Bearer token");

        verify(orderRepository, never())
                .save(any(Order.class));
    }

    @Test
    void shouldConvertCartItemsToOrderItems() {

        CartClient.CartResponse cartResponse =
                new CartClient.CartResponse(
                        1L,
                        25L,
                        List.of(
                                new CartClient.CartItemResponse(
                                        10L,
                                        1L,
                                        "PROD-001",
                                        2),
                                new CartClient.CartItemResponse(
                                        11L,
                                        1L,
                                        "PROD-002",
                                        1)));

        when(productClient.getProduct(
                "PROD-001",
                null))
                .thenReturn(
                        new ProductClient.ProductResponse(
                                "PROD-001",
                                "Product 1",
                                BigDecimal.valueOf(500)));

        when(productClient.getProduct(
                "PROD-002",
                null))
                .thenReturn(
                        new ProductClient.ProductResponse(
                                "PROD-002",
                                "Product 2",
                                BigDecimal.valueOf(1000)));

        Order order = Order.builder()
                .id(1L)
                .userId(25L)
                .status(OrderStatus.CREATED)
                .build();

        List<OrderItem> result =
                orderService.convertCartItemsToOrderItems(
                        cartResponse,
                        order);

        assertEquals(
                2,
                result.size());

        assertEquals(
                "PROD-001",
                result.get(0).getProductId());

        assertEquals(
                2,
                result.get(0).getQuantity());

        assertEquals(
                "Product 1",
                result.get(0).getProductName());

        assertEquals(
                "PROD-002",
                result.get(1).getProductId());

        assertEquals(
                1,
                result.get(1).getQuantity());
    }

@Test
    void shouldCreateOrderFromCartAndCompletePayment() {

        CartClient.CartResponse cartResponse =
                new CartClient.CartResponse(
                        1L, 25L,
                        List.of(
                                new CartClient.CartItemResponse(10L, 1L, "PROD-001", 2),
                                new CartClient.CartItemResponse(11L, 1L, "PROD-002", 1)));

        when(cartClient.getCart("Bearer token")).thenReturn(cartResponse);
        when(inventoryClient.isStockAvailable("PROD-001", 2, "Bearer token")).thenReturn(true);
        when(inventoryClient.isStockAvailable("PROD-002", 1, "Bearer token")).thenReturn(true);
        when(inventoryClient.reserveStock("PROD-001", 2, "Bearer token")).thenReturn(null);
        when(inventoryClient.reserveStock("PROD-002", 1, "Bearer token")).thenReturn(null);
        when(productClient.getProduct("PROD-001", "Bearer token"))
                .thenReturn(new ProductClient.ProductResponse(
                        "PROD-001", "Product 1", BigDecimal.valueOf(500)));
        when(productClient.getProduct("PROD-002", "Bearer token"))
                .thenReturn(new ProductClient.ProductResponse(
                        "PROD-002", "Product 2", BigDecimal.valueOf(1000)));

        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order saved = invocation.getArgument(0);
            if (saved.getId() == null) {
                saved.setId(100L);
            }
            return saved;
        });

        Order checkoutOrder = orderService.createOrderFromCart("Bearer token", 25L);

        assertNotNull(checkoutOrder);
        assertEquals(100L, checkoutOrder.getId());
        assertEquals(25L, checkoutOrder.getUserId());
        assertEquals(BigDecimal.valueOf(2000), checkoutOrder.getTotalAmount());
        assertEquals(OrderStatus.CREATED, checkoutOrder.getStatus());
        assertEquals(2, checkoutOrder.getItems().size());

	// Set checkout details so the invoice mapping can be verified.
        checkoutOrder.setCustomerName("Test Checkout Customer");
        checkoutOrder.setCustomerMobile("9876543210");
        checkoutOrder.setCustomerAddress("12 Test Street");
        checkoutOrder.setCustomerCity("Bengaluru");
        checkoutOrder.setCustomerState("Karnataka");
        checkoutOrder.setCustomerPostalCode("560001");
        checkoutOrder.setCustomerCountry("India");

        verify(paymentClient, never()).processPayment(
                any(), any(), any(), any(), any());

        when(orderRepository.findById(100L)).thenReturn(Optional.of(checkoutOrder));
        when(paymentClient.processPayment(
                100L, 25L, BigDecimal.valueOf(2000),
                "MYSHOPYCART-ORDER-100-USER-25", "UPI", "SUCCESS", "Bearer token"))
                .thenReturn(new PaymentClient.PaymentResponse(
                        1L, 100L, 25L, BigDecimal.valueOf(2000),
                        "COMPLETED", "MYSHOPYCART-ORDER-100-USER-25", "UPI",
                        "DUMMY-GATEWAY-100", null,
                        LocalDateTime.now(), LocalDateTime.now()));

        Order confirmedOrder = orderService.processOrderPayment(
                100L, 25L, "UPI", "SUCCESS", "Bearer token");

        assertNotNull(confirmedOrder);
        assertEquals(OrderStatus.CONFIRMED, confirmedOrder.getStatus());

	// Verify checkout contact details are carried into the invoice.
        ArgumentCaptor<InvoiceCustomerSnapshot> snapshotCaptor =
                ArgumentCaptor.forClass(InvoiceCustomerSnapshot.class);
        ArgumentCaptor<String> paymentMethodCaptor =
                ArgumentCaptor.forClass(String.class);

        verify(invoiceService).createInvoice(
                org.mockito.ArgumentMatchers.eq(confirmedOrder),
                snapshotCaptor.capture(),
                paymentMethodCaptor.capture());

        InvoiceCustomerSnapshot snapshot = snapshotCaptor.getValue();

        assertEquals(
                confirmedOrder.getCustomerMobile(),
                snapshot.customerPhone());

        assertEquals(
                confirmedOrder.getCustomerAddress(),
                snapshot.billingAddress().getAddress());

        assertEquals(
                confirmedOrder.getCustomerCity(),
                snapshot.billingAddress().getCity());

        assertEquals(
                confirmedOrder.getCustomerState(),
                snapshot.billingAddress().getState());

        assertEquals(
                confirmedOrder.getCustomerPostalCode(),
                snapshot.billingAddress().getPostalCode());

        assertEquals("UPI", paymentMethodCaptor.getValue());

        verify(inventoryClient).reserveStock("PROD-001", 2, "Bearer token");
        verify(inventoryClient).reserveStock("PROD-002", 1, "Bearer token");
        verify(paymentClient).processPayment(
                100L, 25L, BigDecimal.valueOf(2000),
                "MYSHOPYCART-ORDER-100-USER-25", "UPI", "SUCCESS", "Bearer token");
        verify(inventoryClient).commitStock("PROD-001", 2, "Bearer token");
        verify(inventoryClient).commitStock("PROD-002", 1, "Bearer token");
        verify(inventoryClient, never()).releaseStock(any(), any(), any());

        ArgumentCaptor<MyShopyCartEvent> eventCaptor =
                ArgumentCaptor.forClass(MyShopyCartEvent.class);
        verify(orderEventProducer, times(2)).publish(eventCaptor.capture());

        List<MyShopyCartEvent> capturedEvents = eventCaptor.getAllValues();
        assertEquals(2, capturedEvents.size());

        MyShopyCartEvent confirmedEvent = capturedEvents.get(1);
        assertEquals("ORDER_CONFIRMED", confirmedEvent.eventType());
        assertEquals(1, confirmedEvent.eventVersion());
        assertEquals("order-service", confirmedEvent.source());
        assertEquals("ORDER", confirmedEvent.aggregateType());
        assertEquals("100", confirmedEvent.aggregateId());
        assertNotNull(confirmedEvent.eventId());
        assertNotNull(confirmedEvent.occurredAt());
        assertNotNull(confirmedEvent.correlationId());
        assertNull(confirmedEvent.causationId());

        OrderConfirmedPayload payload =
                (OrderConfirmedPayload) confirmedEvent.payload();
        assertEquals(100L, payload.orderId());
        assertEquals(25L, payload.userId());
        assertEquals(BigDecimal.valueOf(2000), payload.totalAmount());
    }


@Test
void shouldPropagateInventoryCommitFailureAfterSuccessfulPayment() {
    Order order = Order.builder()
            .id(105L)
            .userId(25L)
            .status(OrderStatus.CREATED)
            .totalAmount(BigDecimal.valueOf(2000))
            .items(List.of(
                    OrderItem.builder()
                            .productId("PROD-001")
                            .quantity(2)
                            .build(),
                    OrderItem.builder()
                            .productId("PROD-002")
                            .quantity(1)
                            .build()))
            .build();

    when(orderRepository.findById(105L))
            .thenReturn(Optional.of(order));

    when(paymentClient.processPayment(
            105L,
            25L,
            BigDecimal.valueOf(2000),
            "MYSHOPYCART-ORDER-105-USER-25",
            "UPI",
            "SUCCESS",
            "Bearer token"))
            .thenReturn(new PaymentClient.PaymentResponse(
                    3L,
                    105L,
                    25L,
                    BigDecimal.valueOf(2000),
                    "COMPLETED",
                    "MYSHOPYCART-ORDER-105-USER-25",
                    "UPI",
                    "DUMMY-GATEWAY-105",
                    null,
                    LocalDateTime.now(),
                    LocalDateTime.now()));

    when(inventoryClient.commitStock(
            "PROD-002", 1, "Bearer token"))
            .thenThrow(new RuntimeException(
                    "Inventory commit failed"));

    RuntimeException exception = assertThrows(
            RuntimeException.class,
            () -> orderService.processOrderPayment(
                    105L,
                    25L,
                    "UPI",
                    "SUCCESS",
                    "Bearer token"));

    assertEquals("Inventory commit failed", exception.getMessage());

    assertEquals(OrderStatus.CREATED, order.getStatus());

    verify(paymentClient).processPayment(
            105L,
            25L,
            BigDecimal.valueOf(2000),
            "MYSHOPYCART-ORDER-105-USER-25",
            "UPI",
            "SUCCESS",
            "Bearer token");

    verify(inventoryClient).commitStock(
            "PROD-001", 2, "Bearer token");

    verify(inventoryClient).commitStock(
            "PROD-002", 1, "Bearer token");

    verify(invoiceService, never()).createInvoice(
            any(Order.class),
            any(InvoiceCustomerSnapshot.class),
            any(String.class));

    verify(orderEventProducer, never()).publish(any());
}


@Test
    void shouldPropagateInvoiceCreationFailureAfterPaymentAndInventoryCommit() {
        Order order = Order.builder()
                .id(106L)
                .userId(25L)
                .status(OrderStatus.CREATED)
                .totalAmount(BigDecimal.valueOf(2000))
                .items(List.of(
                        OrderItem.builder()
                                .productId("PROD-001")
                                .quantity(2)
                                .price(BigDecimal.valueOf(500))
                                .build(),
                        OrderItem.builder()
                                .productId("PROD-002")
                                .quantity(1)
                                .price(BigDecimal.valueOf(1000))
                                .build()))
                .build();

        order.setCustomerName("Test Invoice Customer");
        order.setCustomerEmail("test@example.com");
        order.setCustomerMobile("9876543210");
        order.setCustomerAddress("12 Test Street");
        order.setCustomerCity("Bengaluru");
        order.setCustomerState("Karnataka");
        order.setCustomerPostalCode("560001");
        order.setCustomerCountry("India");

        when(orderRepository.findById(106L))
                .thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(paymentClient.processPayment(
                106L,
                25L,
                BigDecimal.valueOf(2000),
                "MYSHOPYCART-ORDER-106-USER-25",
                "UPI",
                "SUCCESS",
                "Bearer token"))
                .thenReturn(new PaymentClient.PaymentResponse(
                        4L,
                        106L,
                        25L,
                        BigDecimal.valueOf(2000),
                        "COMPLETED",
                        "MYSHOPYCART-ORDER-106-USER-25",
                        "UPI",
                        "DUMMY-GATEWAY-106",
                        null,
                        LocalDateTime.now(),
                        LocalDateTime.now()));

        when(invoiceService.createInvoice(
                any(Order.class),
                any(InvoiceCustomerSnapshot.class),
                any(String.class)))
                .thenThrow(new RuntimeException("Invoice creation failed"));

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> orderService.processOrderPayment(
                        106L,
                        25L,
                        "UPI",
                        "SUCCESS",
                        "Bearer token"));

        assertEquals("Invoice creation failed", exception.getMessage());

        verify(paymentClient).processPayment(
                106L,
                25L,
                BigDecimal.valueOf(2000),
                "MYSHOPYCART-ORDER-106-USER-25",
                "UPI",
                "SUCCESS",
                "Bearer token");

        verify(inventoryClient).commitStock(
                "PROD-001", 2, "Bearer token");
        verify(inventoryClient).commitStock(
                "PROD-002", 1, "Bearer token");

        verify(invoiceService).createInvoice(
                any(Order.class),
                any(InvoiceCustomerSnapshot.class),
                any(String.class));
        verify(invoicePdfGenerator, never()).generate(
                any(com.myshopycart.orderservice.entity.Invoice.class));
        verify(minioStorageService, never()).storeInvoicePdf(
                any(String.class), any(byte[].class));
        verify(orderEventProducer, never()).publish(any());
    }

@Test
    void shouldReleaseInventoryWhenPaymentFails() {

        CartClient.CartResponse cartResponse =
                new CartClient.CartResponse(
                        1L, 25L,
                        List.of(new CartClient.CartItemResponse(
                                10L, 1L, "PROD-001", 2)));

        when(cartClient.getCart("Bearer token")).thenReturn(cartResponse);
        when(inventoryClient.isStockAvailable("PROD-001", 2, "Bearer token"))
                .thenReturn(true);
        when(inventoryClient.reserveStock("PROD-001", 2, "Bearer token"))
                .thenReturn(null);
        when(productClient.getProduct("PROD-001", "Bearer token"))
                .thenReturn(new ProductClient.ProductResponse(
                        "PROD-001", "Product 1", BigDecimal.valueOf(500)));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order saved = invocation.getArgument(0);
            if (saved.getId() == null) {
                saved.setId(101L);
            }
            return saved;
        });

        Order checkoutOrder = orderService.createOrderFromCart("Bearer token", 25L);
        assertEquals(OrderStatus.CREATED, checkoutOrder.getStatus());

        when(orderRepository.findById(101L)).thenReturn(Optional.of(checkoutOrder));
        when(paymentClient.processPayment(
                101L, 25L, BigDecimal.valueOf(1000),
                "MYSHOPYCART-ORDER-101-USER-25", "CARD", "FAILURE", "Bearer token"))
                .thenReturn(new PaymentClient.PaymentResponse(
                        2L, 101L, 25L, BigDecimal.valueOf(1000),
                        "FAILED", "MYSHOPYCART-ORDER-101-USER-25", "CARD",
                        null, "Payment declined",
                        LocalDateTime.now(), LocalDateTime.now()));

        Order cancelledOrder = orderService.processOrderPayment(
                101L, 25L, "CARD", "FAILURE", "Bearer token");

        assertNotNull(cancelledOrder);
        assertEquals(OrderStatus.CANCELLED, cancelledOrder.getStatus());

        verify(inventoryClient).reserveStock("PROD-001", 2, "Bearer token");
        verify(paymentClient).processPayment(
                101L, 25L, BigDecimal.valueOf(1000),
                "MYSHOPYCART-ORDER-101-USER-25", "CARD", "FAILURE", "Bearer token");
        verify(inventoryClient).releaseStock("PROD-001", 2, "Bearer token");
        verify(inventoryClient, never()).commitStock(any(), any(), any());
    }

@Test
    void shouldReleaseInventoryWhenPaymentServiceThrowsException() {

        CartClient.CartResponse cartResponse =
                new CartClient.CartResponse(
                        1L, 25L,
                        List.of(new CartClient.CartItemResponse(
                                10L, 1L, "PROD-001", 2)));

        when(cartClient.getCart("Bearer token")).thenReturn(cartResponse);
        when(inventoryClient.isStockAvailable("PROD-001", 2, "Bearer token"))
                .thenReturn(true);
        when(inventoryClient.reserveStock("PROD-001", 2, "Bearer token"))
                .thenReturn(null);
        when(productClient.getProduct("PROD-001", "Bearer token"))
                .thenReturn(new ProductClient.ProductResponse(
                        "PROD-001", "Product 1", BigDecimal.valueOf(500)));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order saved = invocation.getArgument(0);
            if (saved.getId() == null) {
                saved.setId(102L);
            }
            return saved;
        });

        Order checkoutOrder = orderService.createOrderFromCart("Bearer token", 25L);
        assertEquals(OrderStatus.CREATED, checkoutOrder.getStatus());

        when(orderRepository.findById(102L)).thenReturn(Optional.of(checkoutOrder));
        when(paymentClient.processPayment(
                102L, 25L, BigDecimal.valueOf(1000),
                "MYSHOPYCART-ORDER-102-USER-25", "UPI", "SUCCESS", "Bearer token"))
                .thenThrow(new RuntimeException("Payment service unavailable"));

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> orderService.processOrderPayment(
                        102L, 25L, "UPI", "SUCCESS", "Bearer token"));

        assertEquals("Payment service unavailable", exception.getMessage());
        assertEquals(OrderStatus.CREATED, checkoutOrder.getStatus());

        verify(inventoryClient).reserveStock("PROD-001", 2, "Bearer token");
        verify(paymentClient).processPayment(
                102L, 25L, BigDecimal.valueOf(1000),
                "MYSHOPYCART-ORDER-102-USER-25", "UPI", "SUCCESS", "Bearer token");
        verify(inventoryClient, never()).releaseStock(any(), any(), any());
        verify(inventoryClient, never()).commitStock(any(), any(), any());
    }

    @Test
    void shouldReleaseAlreadyReservedItemsWhenLaterReservationFails() {

        CartClient.CartResponse cartResponse =
                new CartClient.CartResponse(
                        1L,
                        25L,
                        List.of(
                                new CartClient.CartItemResponse(
                                        10L,
                                        1L,
                                        "PROD-001",
                                        2),
                                new CartClient.CartItemResponse(
                                        11L,
                                        1L,
                                        "PROD-002",
                                        1)));

        when(cartClient.getCart("Bearer token"))
                .thenReturn(cartResponse);

        when(inventoryClient.isStockAvailable(
                "PROD-001",
                2,
                "Bearer token"))
                .thenReturn(true);

        when(inventoryClient.isStockAvailable(
                "PROD-002",
                1,
                "Bearer token"))
                .thenReturn(true);

        when(inventoryClient.reserveStock(
                "PROD-001",
                2,
                "Bearer token"))
                .thenReturn(null);

        when(inventoryClient.reserveStock(
                "PROD-002",
                1,
                "Bearer token"))
                .thenThrow(
                        new RuntimeException(
                                "Inventory reservation failed"));

        when(productClient.getProduct(
                "PROD-001",
                "Bearer token"))
                .thenReturn(
                        new ProductClient.ProductResponse(
                                "PROD-001",
                                "Product 1",
                                BigDecimal.valueOf(500)));

        when(productClient.getProduct(
                "PROD-002",
                "Bearer token"))
                .thenReturn(
                        new ProductClient.ProductResponse(
                                "PROD-002",
                                "Product 2",
                                BigDecimal.valueOf(1000)));

        when(orderRepository.save(any(Order.class)))
                .thenAnswer(invocation -> {
                    Order order =
                            invocation.getArgument(0);

                    if (order.getId() == null) {
                        order.setId(103L);
                    }

                    return order;
                });

        Order order =
                orderService.createOrderFromCart(
                        "Bearer token",
                        25L);

        assertNotNull(order);

        assertEquals(
                103L,
                order.getId());

        assertEquals(
                OrderStatus.CANCELLED,
                order.getStatus());

        verify(inventoryClient)
                .reserveStock(
                        "PROD-001",
                        2,
                        "Bearer token");

        verify(inventoryClient)
                .reserveStock(
                        "PROD-002",
                        1,
                        "Bearer token");

        verify(inventoryClient)
                .releaseStock(
                        "PROD-001",
                        2,
                        "Bearer token");

        verify(paymentClient, never())
                .processPayment(
                        any(),
                        any(),
                        any(),
                        any(),
                        any());

        verify(inventoryClient, never())
                .commitStock(
                        any(),
                        any(),
                        any());
    }

    @Test
    void shouldRejectCheckoutWhenCartDoesNotBelongToAuthenticatedUser() {

        CartClient.CartResponse cartResponse =
                new CartClient.CartResponse(
                        1L,
                        99L,
                        List.of(
                                new CartClient.CartItemResponse(
                                        10L,
                                        1L,
                                        "PROD-001",
                                        2)));

        when(cartClient.getCart("Bearer token"))
                .thenReturn(cartResponse);

        IllegalStateException exception =
                assertThrows(
                        IllegalStateException.class,
                        () -> orderService.createOrderFromCart(
                                "Bearer token",
                                25L));

        assertEquals(
                "Cart does not belong to authenticated user",
                exception.getMessage());

        verify(inventoryClient, never())
                .isStockAvailable(
                        any(),
                        any(),
                        any());

        verify(productClient, never())
                .getProduct(
                        any(),
                        any());

        verify(paymentClient, never())
                .processPayment(
                        any(),
                        any(),
                        any(),
                        any(),
                        any());

        verify(orderRepository, never())
                .save(any(Order.class));
    }

    @Test
    void shouldRejectNullCheckoutAuthorizationHeader() {

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> orderService.createOrderFromCart(
                                null,
                                25L));

        assertEquals(
                "Authorization header must not be null or blank",
                exception.getMessage());

        verify(cartClient, never())
                .getCart(any());

        verify(inventoryClient, never())
                .isStockAvailable(
                        any(),
                        any(),
                        any());

        verify(productClient, never())
                .getProduct(
                        any(),
                        any());

        verify(orderRepository, never())
                .save(any(Order.class));
    }

    @Test
    void shouldRejectBlankCheckoutAuthorizationHeader() {

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> orderService.createOrderFromCart(
                                "   ",
                                25L));

        assertEquals(
                "Authorization header must not be null or blank",
                exception.getMessage());

        verify(cartClient, never())
                .getCart(any());

        verify(inventoryClient, never())
                .isStockAvailable(
                        any(),
                        any(),
                        any());

        verify(productClient, never())
                .getProduct(
                        any(),
                        any());

        verify(orderRepository, never())
                .save(any(Order.class));
    }

    @Test
    void shouldRejectNullCheckoutUserId() {

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> orderService.createOrderFromCart(
                                "Bearer token",
                                null));

        assertEquals(
                "User ID must not be null",
                exception.getMessage());

        verify(cartClient, never())
                .getCart(any());

        verify(inventoryClient, never())
                .isStockAvailable(
                        any(),
                        any(),
                        any());

        verify(productClient, never())
                .getProduct(
                        any(),
                        any());

        verify(orderRepository, never())
                .save(any(Order.class));
    }

    @Test
    void shouldRejectEmptyCart() {

        CartClient.CartResponse cartResponse =
                new CartClient.CartResponse(
                        1L,
                        25L,
                        List.of());

        when(cartClient.getCart("Bearer token"))
                .thenReturn(cartResponse);

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> orderService.createOrderFromCart(
                                "Bearer token",
                                25L));

        assertEquals(
                "Cart must contain at least one item",
                exception.getMessage());

        verify(inventoryClient, never())
                .isStockAvailable(
                        any(),
                        any(),
                        any());

        verify(productClient, never())
                .getProduct(
                        any(),
                        any());

        verify(paymentClient, never())
                .processPayment(
                        any(),
                        any(),
                        any(),
                        any(),
                        any());

        verify(orderRepository, never())
                .save(any(Order.class));
    }

    @Test
    void shouldRejectNullCartItems() {

        CartClient.CartResponse cartResponse =
                new CartClient.CartResponse(
                        1L,
                        25L,
                        null);

        when(cartClient.getCart("Bearer token"))
                .thenReturn(cartResponse);

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> orderService.createOrderFromCart(
                                "Bearer token",
                                25L));

        assertEquals(
                "Cart must contain at least one item",
                exception.getMessage());

        verify(inventoryClient, never())
                .isStockAvailable(
                        any(),
                        any(),
                        any());

        verify(productClient, never())
                .getProduct(
                        any(),
                        any());

        verify(paymentClient, never())
                .processPayment(
                        any(),
                        any(),
                        any(),
                        any(),
                        any());

        verify(orderRepository, never())
                .save(any(Order.class));
    }

    @Test
    void shouldNotCreateOrderWhenInventoryIsUnavailable() {

        CartClient.CartResponse cartResponse =
                new CartClient.CartResponse(
                        1L,
                        25L,
                        List.of(
                                new CartClient.CartItemResponse(
                                        10L,
                                        1L,
                                        "PROD-001",
                                        20)));

        when(cartClient.getCart("Bearer token"))
                .thenReturn(cartResponse);

        when(inventoryClient.isStockAvailable(
                "PROD-001",
                20,
                "Bearer token"))
                .thenReturn(false);

        IllegalStateException exception =
                assertThrows(
                        IllegalStateException.class,
                        () -> orderService.createOrderFromCart(
                                "Bearer token",
                                25L));

        assertEquals(
                "Insufficient inventory for product: PROD-001",
                exception.getMessage());

        verify(productClient, never())
                .getProduct(
                        "PROD-001",
                        "Bearer token");

        verify(orderRepository, never())
                .save(any(Order.class));

        verify(paymentClient, never())
                .processPayment(
                        any(),
                        any(),
                        any(),
                        any(),
                        any());
    }

    @Test
    void shouldRejectInvalidCartQuantity() {

        CartClient.CartResponse cartResponse =
                new CartClient.CartResponse(
                        1L,
                        25L,
                        List.of(
                                new CartClient.CartItemResponse(
                                        10L,
                                        1L,
                                        "PROD-001",
                                        0)));

        when(cartClient.getCart("Bearer token"))
                .thenReturn(cartResponse);

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> orderService.createOrderFromCart(
                                "Bearer token",
                                25L));

        assertEquals(
                "Order quantity must be greater than zero",
                exception.getMessage());

        verify(inventoryClient, never())
                .isStockAvailable(
                        "PROD-001",
                        0,
                        "Bearer token");

        verify(orderRepository, never())
                .save(any(Order.class));
    }

    @Test
    void shouldFindOrderById() {

        Order order = Order.builder()
                .id(1L)
                .userId(25L)
                .status(OrderStatus.CREATED)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        when(orderRepository.findById(1L))
                .thenReturn(Optional.of(order));

        Optional<Order> result =
                orderService.findById(1L);

        assertNotNull(result);

        assertEquals(
                Optional.of(order),
                result);

        verify(orderRepository)
                .findById(1L);
    }

    @Test
    void shouldFindOrderByIdForUserWhenOwnerMatches() {

        Order order = Order.builder()
                .id(1L)
                .userId(25L)
                .status(OrderStatus.CREATED)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        when(orderRepository.findById(1L))
                .thenReturn(Optional.of(order));

        Optional<Order> result =
                orderService.findByIdForUser(
                        1L,
                        25L);

        assertNotNull(result);

        assertEquals(
                Optional.of(order),
                result);

        verify(orderRepository)
                .findById(1L);
    }

    @Test
    void shouldNotFindOrderByIdForUserWhenOwnerDoesNotMatch() {

        Order order = Order.builder()
                .id(1L)
                .userId(25L)
                .status(OrderStatus.CREATED)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        when(orderRepository.findById(1L))
                .thenReturn(Optional.of(order));

        Optional<Order> result =
                orderService.findByIdForUser(
                        1L,
                        99L);

        assertEquals(
                Optional.empty(),
                result);

        verify(orderRepository)
                .findById(1L);
    }

    @Test
    void shouldFindOrdersByUserId() {

        Order order1 = Order.builder()
                .id(1L)
                .userId(25L)
                .status(OrderStatus.CREATED)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        Order order2 = Order.builder()
                .id(2L)
                .userId(25L)
                .status(OrderStatus.CONFIRMED)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        List<Order> orders =
                List.of(order1, order2);

        when(orderRepository
                .findByUserIdOrderByCreatedAtDesc(25L))
                .thenReturn(orders);

        List<Order> result =
                orderService.findByUserId(25L);

        assertNotNull(result);

        assertEquals(
                2,
                result.size());

        assertEquals(
                orders,
                result);

        verify(orderRepository)
                .findByUserIdOrderByCreatedAtDesc(
                        25L);
    }

    @Test
    void shouldGetCartUsingAuthorizationHeader() {

        CartClient.CartResponse cartResponse =
                new CartClient.CartResponse(
                        1L,
                        25L,
                        List.of(
                                new CartClient.CartItemResponse(
                                        10L,
                                        1L,
                                        "PROD-001",
                                        2)));

        when(cartClient.getCart(
                "Bearer token"))
                .thenReturn(cartResponse);

        CartClient.CartResponse result =
                orderService.getCart(
                        "Bearer token");

        assertNotNull(result);

        assertEquals(
                1L,
                result.cartId());

        assertEquals(
                25L,
                result.userId());

        assertEquals(
                1,
                result.items().size());

        verify(cartClient)
                .getCart("Bearer token");
    }

    @Test
    void shouldRejectEmptyOrderItems() {

        CreateOrderRequest request =
                new CreateOrderRequest(
                        List.of());

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> orderService.createOrder(
                                request,
                                25L,
                                "Bearer token"));

        assertEquals(
                "Order must contain at least one item",
                exception.getMessage());

        verify(productClient, never())
                .getProduct(
                        any(),
                        any());

        verify(orderRepository, never())
                .save(any(Order.class));
    }

    @Test
    void shouldRejectNullOrderItems() {

        CreateOrderRequest request =
                new CreateOrderRequest(
                        null);

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> orderService.createOrder(
                                request,
                                25L,
                                "Bearer token"));

        assertEquals(
                "Order must contain at least one item",
                exception.getMessage());

        verify(productClient, never())
                .getProduct(
                        any(),
                        any());

        verify(orderRepository, never())
                .save(any(Order.class));
    }

    @Test
    void shouldRejectNullOrderRequest() {

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> orderService.createOrder(
                                null,
                                25L,
                                "Bearer token"));

        assertEquals(
                "Order request must not be null",
                exception.getMessage());

        verify(productClient, never())
                .getProduct(
                        any(),
                        any());

        verify(orderRepository, never())
                .save(any(Order.class));
    }

    @Test
    void shouldRejectNullUserId() {

        CreateOrderRequest request =
                new CreateOrderRequest(
                        List.of(
                                new OrderItemRequest(
                                        "PROD-001",
                                        1,
                                        BigDecimal.valueOf(500))));

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> orderService.createOrder(
                                request,
                                null,
                                "Bearer token"));

        assertEquals(
                "User ID must not be null",
                exception.getMessage());

        verify(productClient, never())
                .getProduct(
                        any(),
                        any());

        verify(orderRepository, never())
                .save(any(Order.class));
    }

    @Test
    void shouldRejectNullAuthorizationHeader() {

        CreateOrderRequest request =
                new CreateOrderRequest(
                        List.of(
                                new OrderItemRequest(
                                        "PROD-001",
                                        1,
                                        BigDecimal.valueOf(500))));

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> orderService.createOrder(
                                request,
                                25L,
                                null));

        assertEquals(
                "Authorization header must not be null or blank",
                exception.getMessage());

        verify(productClient, never())
                .getProduct(
                        any(),
                        any());

        verify(orderRepository, never())
                .save(any(Order.class));
    }

    @Test
    void shouldRejectBlankAuthorizationHeader() {

        CreateOrderRequest request =
                new CreateOrderRequest(
                        List.of(
                                new OrderItemRequest(
                                        "PROD-001",
                                        1,
                                        BigDecimal.valueOf(500))));

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> orderService.createOrder(
                                request,
                                25L,
                                "   "));

        assertEquals(
                "Authorization header must not be null or blank",
                exception.getMessage());

        verify(productClient, never())
                .getProduct(
                        any(),
                        any());

        verify(orderRepository, never())
                .save(any(Order.class));
    }

    @Test
    void shouldRejectOrderWhenProductDoesNotExist() {

        OrderItemRequest item =
                new OrderItemRequest(
                        "PROD-404",
                        2,
                        BigDecimal.valueOf(500));

        CreateOrderRequest request =
                new CreateOrderRequest(
                        List.of(item),
                        1L,
                        2L);

        when(productClient.getProduct(
                "PROD-404",
                "Bearer token"))
                .thenThrow(
                        new IllegalArgumentException(
                                "Product not found: PROD-404"));

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> orderService.createOrder(
                                request,
                                25L,
                                "Bearer token"));

        assertEquals(
                "Product not found: PROD-404",
                exception.getMessage());

        verify(orderRepository, never())
                .save(any(Order.class));
    }

    @Test
    void shouldAllowCreatedToConfirmed() {

        Order order = Order.builder()
                .id(1L)
                .userId(25L)
                .status(OrderStatus.CREATED)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        when(orderRepository.findById(1L))
                .thenReturn(Optional.of(order));

        when(orderStatusTransition.isAllowed(
                OrderStatus.CREATED,
                OrderStatus.CONFIRMED))
                .thenReturn(true);

        when(orderRepository.save(any(Order.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0));

        Order updatedOrder =
                orderService.updateStatus(
                        1L,
                        OrderStatus.CONFIRMED);

        assertEquals(
                OrderStatus.CONFIRMED,
                updatedOrder.getStatus());

        verify(orderRepository)
                .save(order);
    }

    @Test
    void shouldRejectInvalidStatusTransition() {

        Order order = Order.builder()
                .id(1L)
                .userId(25L)
                .status(OrderStatus.CREATED)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        when(orderRepository.findById(1L))
                .thenReturn(Optional.of(order));

        when(orderStatusTransition.isAllowed(
                OrderStatus.CREATED,
                OrderStatus.SHIPPED))
                .thenReturn(false);

        IllegalStateException exception =
                assertThrows(
                        IllegalStateException.class,
                        () -> orderService.updateStatus(
                                1L,
                                OrderStatus.SHIPPED));

        assertEquals(
                "Invalid order status transition from CREATED to SHIPPED",
                exception.getMessage());

        verify(orderRepository, never())
                .save(any(Order.class));
    }

    @Test
    void shouldNotSaveOrderWhenStatusTransitionIsInvalid() {

        Instant updatedAt =
                Instant.parse("2026-01-01T10:00:00Z");

        Order order =
                Order.builder()
                        .id(1L)
                        .userId(25L)
                        .status(OrderStatus.CREATED)
                        .createdAt(updatedAt)
                        .updatedAt(updatedAt)
                        .build();

        when(orderRepository.findById(1L))
                .thenReturn(Optional.of(order));

        when(orderStatusTransition.isAllowed(
                OrderStatus.CREATED,
                OrderStatus.SHIPPED))
                .thenReturn(false);

        IllegalStateException exception =
                assertThrows(
                        IllegalStateException.class,
                        () -> orderService.updateStatus(
                                1L,
                                OrderStatus.SHIPPED));

        assertEquals(
                "Invalid order status transition from CREATED to SHIPPED",
                exception.getMessage());

        assertEquals(
                OrderStatus.CREATED,
                order.getStatus());

        assertEquals(
                updatedAt,
                order.getUpdatedAt());

        verify(orderRepository, never())
                .save(any(Order.class));
    }

    @Test
    void shouldNotUpdateDeliveredOrderStatus() {

        Instant updatedAt =
                Instant.parse("2026-01-01T10:00:00Z");

        Order order =
                Order.builder()
                        .id(1L)
                        .userId(25L)
                        .status(OrderStatus.DELIVERED)
                        .createdAt(updatedAt)
                        .updatedAt(updatedAt)
                        .build();

        when(orderRepository.findById(1L))
                .thenReturn(Optional.of(order));

        when(orderStatusTransition.isAllowed(
                OrderStatus.DELIVERED,
                OrderStatus.SHIPPED))
                .thenReturn(false);

        IllegalStateException exception =
                assertThrows(
                        IllegalStateException.class,
                        () -> orderService.updateStatus(
                                1L,
                                OrderStatus.SHIPPED));

        assertEquals(
                "Invalid order status transition from DELIVERED to SHIPPED",
                exception.getMessage());

        assertEquals(
                OrderStatus.DELIVERED,
                order.getStatus());

        assertEquals(
                updatedAt,
                order.getUpdatedAt());

        verify(orderRepository, never())
                .save(any(Order.class));
    }

    @Test
    void shouldNotUpdateCancelledOrderStatus() {

        Instant updatedAt =
                Instant.parse("2026-01-01T10:00:00Z");

        Order order =
                Order.builder()
                        .id(1L)
                        .userId(25L)
                        .status(OrderStatus.CANCELLED)
                        .createdAt(updatedAt)
                        .updatedAt(updatedAt)
                        .build();

        when(orderRepository.findById(1L))
                .thenReturn(Optional.of(order));

        when(orderStatusTransition.isAllowed(
                OrderStatus.CANCELLED,
                OrderStatus.CONFIRMED))
                .thenReturn(false);

        IllegalStateException exception =
                assertThrows(
                        IllegalStateException.class,
                        () -> orderService.updateStatus(
                                1L,
                                OrderStatus.CONFIRMED));

        assertEquals(
                "Invalid order status transition from CANCELLED to CONFIRMED",
                exception.getMessage());

        assertEquals(
                OrderStatus.CANCELLED,
                order.getStatus());

        assertEquals(
                updatedAt,
                order.getUpdatedAt());

        verify(orderRepository, never())
                .save(any(Order.class));
    }

    @Test
    void shouldRejectStatusUpdateWhenOrderDoesNotExist() {

        when(orderRepository.findById(999L))
                .thenReturn(Optional.empty());

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> orderService.updateStatus(
                                999L,
                                OrderStatus.CONFIRMED));

        assertEquals(
                "Order not found: 999",
                exception.getMessage());

        verify(orderRepository, never())
                .save(any(Order.class));
    }

    @Test
    void shouldNotCheckTransitionWhenOrderDoesNotExist() {

        when(orderRepository.findById(999L))
                .thenReturn(Optional.empty());

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> orderService.updateStatus(
                                999L,
                                OrderStatus.CONFIRMED));

        assertEquals(
                "Order not found: 999",
                exception.getMessage());

        verify(orderStatusTransition, never())
                .isAllowed(
                        any(OrderStatus.class),
                        any(OrderStatus.class));

        verify(orderRepository, never())
                .save(any(Order.class));
    }

    @Test
    void shouldRejectNullOrderId() {

        when(orderRepository.findById(null))
                .thenReturn(Optional.empty());

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> orderService.updateStatus(
                                null,
                                OrderStatus.CONFIRMED));

        assertEquals(
                "Order not found: null",
                exception.getMessage());

        verify(orderStatusTransition, never())
                .isAllowed(
                        any(OrderStatus.class),
                        any(OrderStatus.class));

        verify(orderRepository, never())
                .save(any(Order.class));
    }

    @Test
    void shouldRejectNullOrderStatus() {

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> orderService.updateStatus(
                                1L,
                                null));

        assertEquals(
                "Order status must not be null",
                exception.getMessage());

        verify(orderRepository, never())
                .findById(any());

        verify(orderStatusTransition, never())
                .isAllowed(
                        any(OrderStatus.class),
                        any(OrderStatus.class));

        verify(orderRepository, never())
                .save(any(Order.class));
    }

    @Test
    void shouldRejectNullOrderStatusBeforeRepositoryLookup() {

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> orderService.updateStatus(
                                1L,
                                null));

        assertEquals(
                "Order status must not be null",
                exception.getMessage());

        verify(orderRepository, never())
                .findById(1L);

        verify(orderStatusTransition, never())
                .isAllowed(
                        any(OrderStatus.class),
                        any(OrderStatus.class));

        verify(orderRepository, never())
                .save(any(Order.class));
    }

@Test
    void shouldCheckInventoryForEveryCartItemBeforeSavingOrder() {

        CartClient.CartResponse cartResponse =
                new CartClient.CartResponse(
                        1L, 25L,
                        List.of(
                                new CartClient.CartItemResponse(10L, 1L, "PROD-001", 2),
                                new CartClient.CartItemResponse(11L, 1L, "PROD-002", 3)));

        when(cartClient.getCart("Bearer token")).thenReturn(cartResponse);
        when(inventoryClient.isStockAvailable("PROD-001", 2, "Bearer token"))
                .thenReturn(true);
        when(inventoryClient.isStockAvailable("PROD-002", 3, "Bearer token"))
                .thenReturn(true);
        when(productClient.getProduct("PROD-001", "Bearer token"))
                .thenReturn(new ProductClient.ProductResponse(
                        "PROD-001", "Product 1", BigDecimal.valueOf(500)));
        when(productClient.getProduct("PROD-002", "Bearer token"))
                .thenReturn(new ProductClient.ProductResponse(
                        "PROD-002", "Product 2", BigDecimal.valueOf(1000)));
        when(inventoryClient.reserveStock("PROD-001", 2, "Bearer token"))
                .thenReturn(null);
        when(inventoryClient.reserveStock("PROD-002", 3, "Bearer token"))
                .thenReturn(null);
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order saved = invocation.getArgument(0);
            if (saved.getId() == null) {
                saved.setId(104L);
            }
            return saved;
        });

        Order order = orderService.createOrderFromCart("Bearer token", 25L);

        assertNotNull(order);
        assertEquals(104L, order.getId());
        assertEquals(BigDecimal.valueOf(4000), order.getTotalAmount());
        assertEquals(OrderStatus.CREATED, order.getStatus());
        assertEquals(2, order.getItems().size());

        verify(inventoryClient).isStockAvailable("PROD-001", 2, "Bearer token");
        verify(inventoryClient).isStockAvailable("PROD-002", 3, "Bearer token");
        verify(productClient).getProduct("PROD-001", "Bearer token");
        verify(productClient).getProduct("PROD-002", "Bearer token");
        verify(inventoryClient).reserveStock("PROD-001", 2, "Bearer token");
        verify(inventoryClient).reserveStock("PROD-002", 3, "Bearer token");

        verify(paymentClient, never()).processPayment(
                any(), any(), any(), any(), any());
        verify(inventoryClient, never()).commitStock(any(), any(), any());
    }

    @Test
    void shouldNotSaveOrderWhenLaterCartItemHasInsufficientInventory() {

        CartClient.CartResponse cartResponse =
                new CartClient.CartResponse(
                        1L,
                        25L,
                        List.of(
                                new CartClient.CartItemResponse(
                                        10L,
                                        1L,
                                        "PROD-001",
                                        2),
                                new CartClient.CartItemResponse(
                                        11L,
                                        1L,
                                        "PROD-002",
                                        5)));

        when(cartClient.getCart("Bearer token"))
                .thenReturn(cartResponse);

        when(inventoryClient.isStockAvailable(
                "PROD-001",
                2,
                "Bearer token"))
                .thenReturn(true);

        when(inventoryClient.isStockAvailable(
                "PROD-002",
                5,
                "Bearer token"))
                .thenReturn(false);

        when(productClient.getProduct(
                "PROD-001",
                "Bearer token"))
                .thenReturn(
                        new ProductClient.ProductResponse(
                                "PROD-001",
                                "Product 1",
                                BigDecimal.valueOf(500)));

        IllegalStateException exception =
                assertThrows(
                        IllegalStateException.class,
                        () -> orderService.createOrderFromCart(
                                "Bearer token",
                                25L));

        assertEquals(
                "Insufficient inventory for product: PROD-002",
                exception.getMessage());

        verify(inventoryClient)
                .isStockAvailable(
                        "PROD-001",
                        2,
                        "Bearer token");

        verify(inventoryClient)
                .isStockAvailable(
                        "PROD-002",
                        5,
                        "Bearer token");

        verify(productClient)
                .getProduct(
                        "PROD-001",
                        "Bearer token");

        verify(productClient, never())
                .getProduct(
                        "PROD-002",
                        "Bearer token");

        verify(orderRepository, never())
                .save(any(Order.class));
    }

    @Test
    void shouldUpdateUpdatedAtWhenOrderStatusChanges() {

        Instant oldUpdatedAt =
                Instant.parse("2026-01-01T10:00:00Z");

        Order order =
                Order.builder()
                        .id(1L)
                        .userId(25L)
                        .status(OrderStatus.CREATED)
                        .createdAt(oldUpdatedAt)
                        .updatedAt(oldUpdatedAt)
                        .build();

        when(orderRepository.findById(1L))
                .thenReturn(Optional.of(order));

        when(orderStatusTransition.isAllowed(
                OrderStatus.CREATED,
                OrderStatus.CONFIRMED))
                .thenReturn(true);

        when(orderRepository.save(any(Order.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0));

        Order updatedOrder =
                orderService.updateStatus(
                        1L,
                        OrderStatus.CONFIRMED);

        assertEquals(
                OrderStatus.CONFIRMED,
                updatedOrder.getStatus());

        assertNotNull(
                updatedOrder.getUpdatedAt());

        assertTrue(
                updatedOrder.getUpdatedAt()
                        .isAfter(oldUpdatedAt));

        verify(orderRepository)
                .save(order);
    }

    @Test
    void shouldSaveOrderWithUpdatedStatusAndTimestamp() {

        Instant oldUpdatedAt =
                Instant.parse("2026-01-01T10:00:00Z");

        Order order =
                Order.builder()
                        .id(1L)
                        .userId(25L)
                        .status(OrderStatus.CREATED)
                        .createdAt(oldUpdatedAt)
                        .updatedAt(oldUpdatedAt)
                        .build();

        when(orderRepository.findById(1L))
                .thenReturn(Optional.of(order));

        when(orderStatusTransition.isAllowed(
                OrderStatus.CREATED,
                OrderStatus.CONFIRMED))
                .thenReturn(true);

        when(orderRepository.save(any(Order.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0));

        Order updatedOrder =
                orderService.updateStatus(
                        1L,
                        OrderStatus.CONFIRMED);

        assertEquals(
                OrderStatus.CONFIRMED,
                updatedOrder.getStatus());

        assertNotNull(
                updatedOrder.getUpdatedAt());

        assertTrue(
                updatedOrder.getUpdatedAt()
                        .isAfter(oldUpdatedAt));

        verify(orderRepository)
                .save(updatedOrder);
    }
    
        @Test
    void shouldDownloadInvoiceForOrderOwner() throws Exception {

        Long orderId = 200L;
        Long userId = 25L;

        Order order = Order.builder()
                .id(orderId)
                .userId(userId)
                .status(OrderStatus.CONFIRMED)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        com.myshopycart.orderservice.entity.Invoice invoice =
                org.mockito.Mockito.mock(
                        com.myshopycart.orderservice.entity.Invoice.class);

        byte[] pdfBytes =
                "%PDF-1.4 MYSHOPYCART INVOICE"
                        .getBytes();

        when(orderRepository.findById(orderId))
                .thenReturn(Optional.of(order));

        when(invoiceService.getInvoiceByOrderId(orderId))
                .thenReturn(invoice);

        when(invoice.getInvoiceNumber())
                .thenReturn("INV-TEST-200");

        when(minioStorageService.getInvoicePdf(
                "INV-TEST-200"))
                .thenReturn(
                        new java.io.ByteArrayInputStream(
                                pdfBytes));

        Optional<InvoiceDownloadData> result =
                orderService.getInvoiceDownloadForUser(
                        orderId,
                        userId);

        assertTrue(result.isPresent());

        InvoiceDownloadData downloadData =
                result.get();

        assertEquals(
                "INV-TEST-200",
                downloadData.invoiceNumber());

        assertArrayEquals(
                pdfBytes,
                downloadData.pdfBytes());

        verify(orderRepository)
                .findById(orderId);

        verify(invoiceService)
                .getInvoiceByOrderId(orderId);

        verify(minioStorageService)
                .getInvoicePdf("INV-TEST-200");
    }

    @Test
    void shouldNotDownloadInvoiceWhenOrderBelongsToAnotherUser() {

        Long orderId = 201L;

        Order order = Order.builder()
                .id(orderId)
                .userId(25L)
                .status(OrderStatus.CONFIRMED)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        when(orderRepository.findById(orderId))
                .thenReturn(Optional.of(order));

        Optional<InvoiceDownloadData> result =
                orderService.getInvoiceDownloadForUser(
                        orderId,
                        99L);

        assertTrue(result.isEmpty());

        verify(orderRepository)
                .findById(orderId);

        verify(invoiceService, never())
                .getInvoiceByOrderId(any());

        verify(minioStorageService, never())
                .getInvoicePdf(any());
    }

    @Test
    void shouldReturnEmptyWhenOrderDoesNotExist() {

        Long orderId = 999L;

        when(orderRepository.findById(orderId))
                .thenReturn(Optional.empty());

        Optional<InvoiceDownloadData> result =
                orderService.getInvoiceDownloadForUser(
                        orderId,
                        25L);

        assertTrue(result.isEmpty());

        verify(orderRepository)
                .findById(orderId);

        verify(invoiceService, never())
                .getInvoiceByOrderId(any());

        verify(minioStorageService, never())
                .getInvoicePdf(any());
    }

    @Test
    void shouldRejectInvalidInvoiceDownloadOrderId() {

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> orderService
                                .getInvoiceDownloadForUser(
                                        null,
                                        25L));

        assertEquals(
                "Order ID must be valid",
                exception.getMessage());

        verify(orderRepository, never())
                .findById(any());
    }

    @Test
    void shouldRejectInvalidInvoiceDownloadUserId() {

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> orderService
                                .getInvoiceDownloadForUser(
                                        200L,
                                        null));

        assertEquals(
                "User ID must be valid",
                exception.getMessage());

        verify(orderRepository, never())
                .findById(any());
    }
}