package com.myshopycart.orderservice.service;

import org.springframework.beans.factory.annotation.Autowired;
import com.myshopycart.orderservice.client.CartClient;
import com.myshopycart.orderservice.client.InventoryClient;
import com.myshopycart.orderservice.client.PaymentClient;
import com.myshopycart.orderservice.client.AuthAddressClient;
import com.myshopycart.orderservice.client.ProductClient;
import com.myshopycart.orderservice.dto.CheckoutRequest;
import com.myshopycart.orderservice.dto.CreateOrderRequest;
import com.myshopycart.orderservice.dto.OrderItemRequest;
import com.myshopycart.orderservice.dto.InvoiceDetailsResponse;
import com.myshopycart.orderservice.entity.Order;
import com.myshopycart.orderservice.entity.OrderItem;
import com.myshopycart.orderservice.entity.OrderStatus;
import com.myshopycart.orderservice.entity.Invoice;
import com.myshopycart.orderservice.entity.InvoiceAddress;
import com.myshopycart.orderservice.entity.InvoiceItem;
import com.myshopycart.orderservice.messaging.event.MyShopyCartEvent;
import com.myshopycart.orderservice.messaging.event.OrderCancelledPayload;
import com.myshopycart.orderservice.messaging.event.OrderConfirmedPayload;
import com.myshopycart.orderservice.messaging.event.OrderCreatedPayload;
import com.myshopycart.orderservice.messaging.producer.OrderEventProducer;
import com.myshopycart.orderservice.repository.OrderRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
public class OrderService {

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

    @Autowired
    public OrderService(
            OrderRepository orderRepository,
            OrderStatusTransition orderStatusTransition,
            CartClient cartClient,
            ProductClient productClient,
            InventoryClient inventoryClient,
            PaymentClient paymentClient,
            OrderEventProducer orderEventProducer,
            AuthAddressClient authAddressClient,
            InvoiceService invoiceService,
            InvoicePdfGenerator invoicePdfGenerator,
            MinioStorageService minioStorageService) {
        this.orderRepository = orderRepository;
        this.orderStatusTransition = orderStatusTransition;
        this.cartClient = cartClient;
        this.productClient = productClient;
        this.inventoryClient = inventoryClient;
        this.paymentClient = paymentClient;
        this.orderEventProducer = orderEventProducer;
        this.authAddressClient = authAddressClient;
        this.invoiceService = invoiceService;
        this.invoicePdfGenerator = invoicePdfGenerator;
        this.minioStorageService = minioStorageService;
    }

    // Backward-compatible constructor for existing unit tests.
    public OrderService(
            OrderRepository orderRepository,
            OrderStatusTransition orderStatusTransition,
            CartClient cartClient,
            ProductClient productClient,
            InventoryClient inventoryClient,
            PaymentClient paymentClient,
            OrderEventProducer orderEventProducer,
            AuthAddressClient authAddressClient) {
        this(orderRepository, orderStatusTransition, cartClient, productClient,
                inventoryClient, paymentClient, orderEventProducer,
                authAddressClient, null, null, null);
    }

    // Backward-compatible constructor for older unit tests.
    public OrderService(
            OrderRepository orderRepository,
            OrderStatusTransition orderStatusTransition,
            CartClient cartClient,
            ProductClient productClient,
            InventoryClient inventoryClient,
            PaymentClient paymentClient,
            OrderEventProducer orderEventProducer) {
        this(orderRepository, orderStatusTransition, cartClient, productClient,
                inventoryClient, paymentClient, orderEventProducer,
                null, null, null, null);
    }

    public Optional<Order> findById(Long orderId) {
        return orderRepository.findById(orderId);
    }

    public Optional<Order> findByIdForUser(Long orderId, Long userId) {
        return orderRepository.findById(orderId)
                .filter(order -> order.getUserId().equals(userId));
    }

    public List<Order> findByUserId(Long userId) {
        return orderRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    /** Returns invoice details only when the authenticated user owns the order. */
    @Transactional(readOnly = true)
    public Optional<InvoiceDetailsResponse> getInvoiceDetailsForUser(
            Long orderId, Long userId) {
        if (orderId == null || orderId <= 0) {
            throw new IllegalArgumentException("Order ID must be valid");
        }
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("User ID must be valid");
        }

        Optional<Order> ownedOrder = orderRepository.findById(orderId)
                .filter(order -> userId.equals(order.getUserId()));
        if (ownedOrder.isEmpty()) {
            return Optional.empty();
        }

        final Invoice invoice;
        try {
            invoice = invoiceService.getInvoiceByOrderId(orderId);
        } catch (IllegalArgumentException exception) {
            return Optional.empty();
        }
        if (invoice == null || invoice.getInvoiceNumber() == null
                || invoice.getInvoiceNumber().isBlank()) {
            return Optional.empty();
        }

        List<InvoiceDetailsResponse.ItemResponse> items =
                invoice.getItems() == null
                        ? List.of()
                        : invoice.getItems().stream()
                                .map(this::toInvoiceItemResponse)
                                .toList();

        return Optional.of(new InvoiceDetailsResponse(
                invoice.getInvoiceNumber(),
                invoice.getOrderId(),
                invoice.getIssueDate() == null ? null : invoice.getIssueDate().toString(),
                invoice.getCustomerName(),
                invoice.getCustomerEmail(),
                invoice.getCustomerPhone(),
                invoice.getCustomerGstin(),
                toInvoiceAddressResponse(invoice.getBillingAddress()),
                toInvoiceAddressResponse(invoice.getShippingAddress()),
                invoice.getPaymentMethod(),
                invoice.getSubtotal(),
                invoice.getTaxAmount(),
                invoice.getTotalAmount(),
                items));
    }

    private InvoiceDetailsResponse.AddressResponse toInvoiceAddressResponse(
            InvoiceAddress address) {
        if (address == null) {
            return null;
        }
        return new InvoiceDetailsResponse.AddressResponse(
                address.getAddress(), address.getCity(), address.getState(),
                address.getPostalCode(), address.getCountry());
    }

    private InvoiceDetailsResponse.ItemResponse toInvoiceItemResponse(InvoiceItem item) {
        return new InvoiceDetailsResponse.ItemResponse(
                item.getProductId(), item.getProductName(), item.getQuantity(),
                item.getUnitPrice(), item.getLineTotal());
    }

    public Optional<InvoiceDownloadData> getInvoiceDownloadForUser(
            Long orderId, Long userId) {
        if (orderId == null || orderId <= 0) {
            throw new IllegalArgumentException("Order ID must be valid");
        }
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("User ID must be valid");
        }

        Optional<Order> order = orderRepository.findById(orderId)
                .filter(existingOrder -> userId.equals(existingOrder.getUserId()));
        if (order.isEmpty()) {
            return Optional.empty();
        }

        Invoice invoice = invoiceService.getInvoiceByOrderId(orderId);
        if (invoice == null || invoice.getInvoiceNumber() == null
                || invoice.getInvoiceNumber().isBlank()) {
            throw new IllegalStateException(
                    "Invoice number not available for order: " + orderId);
        }
        if (minioStorageService == null) {
            throw new IllegalStateException("MinIO storage service is not configured");
        }

        try (java.io.InputStream inputStream =
                     minioStorageService.getInvoicePdf(invoice.getInvoiceNumber())) {
            byte[] pdfBytes = inputStream.readAllBytes();
            if (pdfBytes.length == 0) {
                throw new IllegalStateException(
                        "Invoice PDF is empty for order: " + orderId);
            }
            return Optional.of(new InvoiceDownloadData(
                    invoice.getInvoiceNumber(), pdfBytes));
        } catch (java.io.IOException exception) {
            throw new IllegalStateException(
                    "Unable to read invoice PDF for order: " + orderId, exception);
        }
    }

    @Transactional
    public Order createOrder(
            CreateOrderRequest request, Long userId, String authorizationHeader) {
        if (request == null) {
            throw new IllegalArgumentException("Order request must not be null");
        }
        if (userId == null) {
            throw new IllegalArgumentException("User ID must not be null");
        }
        if (authorizationHeader == null || authorizationHeader.isBlank()) {
            throw new IllegalArgumentException(
                    "Authorization header must not be null or blank");
        }
        if (request.items() == null || request.items().isEmpty()) {
            throw new IllegalArgumentException("Order must contain at least one item");
        }
        if (request.shippingAddressId() == null) {
            throw new IllegalArgumentException("Shipping address is required");
        }
        if (request.billingAddressId() == null) {
            throw new IllegalArgumentException("Billing address is required");
        }
        if (authAddressClient == null) {
            throw new IllegalStateException("Auth address client is not available");
        }

        AuthAddressClient.UserAddressResponse shippingAddress =
                authAddressClient.getAddress(request.shippingAddressId(), authorizationHeader);
        if (shippingAddress == null) {
            throw new IllegalArgumentException("Shipping address not found");
        }
        if (!userId.equals(shippingAddress.userId())) {
            throw new IllegalArgumentException(
                    "Shipping address does not belong to the authenticated user");
        }

        AuthAddressClient.UserAddressResponse billingAddress =
                authAddressClient.getAddress(request.billingAddressId(), authorizationHeader);
        if (billingAddress == null) {
            throw new IllegalArgumentException("Billing address not found");
        }
        if (!userId.equals(billingAddress.userId())) {
            throw new IllegalArgumentException(
                    "Billing address does not belong to the authenticated user");
        }

        AuthAddressClient.CustomerContactResponse customerContact =
                getCustomerContact(userId, authorizationHeader);
        Instant now = Instant.now();
        Order order = Order.builder()
                .userId(userId)
                .customerName(customerContact.username())
                .customerEmail(customerContact.email())
                .customerMobile(customerContact.mobileNumber())
                .status(OrderStatus.CREATED)
                .createdAt(now)
                .updatedAt(now)
                .items(new ArrayList<>())
                .build();

        BigDecimal totalAmount = BigDecimal.ZERO;
        for (OrderItemRequest itemRequest : request.items()) {
            validateQuantity(itemRequest.quantity());
            ProductClient.ProductResponse product = productClient.getProduct(
                    itemRequest.productId(), authorizationHeader);
            BigDecimal price = product.price();
            totalAmount = totalAmount.add(
                    price.multiply(BigDecimal.valueOf(itemRequest.quantity())));
            OrderItem orderItem = OrderItem.builder()
                    .order(order)
                    .productId(itemRequest.productId())
                    .productName(product.name())
                    .quantity(itemRequest.quantity())
                    .price(price)
                    .createdAt(now)
                    .build();
            order.getItems().add(orderItem);
        }
        order.setTotalAmount(totalAmount);
        return orderRepository.save(order);
    }

    private AuthAddressClient.CustomerContactResponse getCustomerContact(
            Long userId, String authorizationHeader) {
        if (authAddressClient == null) {
            throw new IllegalStateException("Auth address client is not available");
        }
        AuthAddressClient.CustomerContactResponse customerContact =
                authAddressClient.getCustomerContact(userId, authorizationHeader);
        if (customerContact == null) {
            throw new IllegalArgumentException("Customer contact not found");
        }
        if (!userId.equals(customerContact.userId())) {
            throw new IllegalArgumentException(
                    "Customer contact does not belong to the authenticated user");
        }
        return customerContact;
    }

    public CartClient.CartResponse getCart(String authorizationHeader) {
        return cartClient.getCart(authorizationHeader);
    }

    public List<OrderItem> convertCartItemsToOrderItems(
            CartClient.CartResponse cartResponse, Order order) {
        Instant now = Instant.now();
        List<OrderItem> orderItems = new ArrayList<>();
        for (CartClient.CartItemResponse cartItem : cartResponse.items()) {
            ProductClient.ProductResponse product =
                    productClient.getProduct(cartItem.productId(), null);
            OrderItem orderItem = OrderItem.builder()
                    .order(order)
                    .productId(cartItem.productId())
                    .productName(product.name())
                    .quantity(cartItem.quantity())
                    .price(product.price())
                    .createdAt(now)
                    .build();
            orderItems.add(orderItem);
        }
        return orderItems;
    }

    /** Creates an order and reserves stock; payment happens separately. */
    @Transactional
    public Order createOrderFromCart(
            CheckoutRequest request,
            String authorizationHeader,
            Long userId) {
        log.info("CHECKOUT START - userId={}", userId);
        if (authorizationHeader == null || authorizationHeader.isBlank()) {
            throw new IllegalArgumentException(
                    "Authorization header must not be null or blank");
        }
        if (userId == null) {
            throw new IllegalArgumentException("User ID must not be null");
        }

        CartClient.CartResponse cartResponse = cartClient.getCart(authorizationHeader);
        if (cartResponse == null || cartResponse.items() == null
                || cartResponse.items().isEmpty()) {
            throw new IllegalArgumentException("Cart must contain at least one item");
        }
        if (!userId.equals(cartResponse.userId())) {
            throw new IllegalStateException(
                    "Cart does not belong to authenticated user");
        }

        AuthAddressClient.CustomerContactResponse customerContact =
                getCustomerContact(userId, authorizationHeader);
        Instant now = Instant.now();
        Order order = Order.builder()
                .userId(userId)
                .customerName(request == null
                        ? customerContact.username() : request.fullName().trim())
                .customerEmail(customerContact.email())
                .customerMobile(request == null
                        ? customerContact.mobileNumber() : request.phone().trim())
                .customerAddress(request == null ? null : request.address().trim())
                .customerCity(request == null ? null : request.city().trim())
                .customerState(request == null ? null : request.state().trim())
                .customerPostalCode(request == null ? null : request.pincode().trim())
                .customerCountry(request == null ? null : "India")
                .status(OrderStatus.CREATED)
                .createdAt(now)
                .updatedAt(now)
                .items(new ArrayList<>())
                .build();

        BigDecimal totalAmount = BigDecimal.ZERO;
        for (CartClient.CartItemResponse cartItem : cartResponse.items()) {
            validateQuantity(cartItem.quantity());
            boolean available = inventoryClient.isStockAvailable(
                    cartItem.productId(), cartItem.quantity(), authorizationHeader);
            if (!available) {
                throw new IllegalStateException(
                        "Insufficient inventory for product: " + cartItem.productId());
            }
            ProductClient.ProductResponse product = productClient.getProduct(
                    cartItem.productId(), authorizationHeader);
            BigDecimal price = product.price();
            totalAmount = totalAmount.add(
                    price.multiply(BigDecimal.valueOf(cartItem.quantity())));
            order.getItems().add(OrderItem.builder()
                    .order(order)
                    .productId(cartItem.productId())
                    .productName(product.name())
                    .quantity(cartItem.quantity())
                    .price(price)
                    .createdAt(now)
                    .build());
        }
        order.setTotalAmount(totalAmount);
        order = orderRepository.save(order);

        OrderCreatedPayload createdPayload = new OrderCreatedPayload(
                order.getId(), order.getUserId(), order.getTotalAmount());
        MyShopyCartEvent<OrderCreatedPayload> createdEvent = new MyShopyCartEvent<>(
                UUID.randomUUID().toString(), "ORDER_CREATED", 1, Instant.now(),
                "order-service", "ORDER", String.valueOf(order.getId()),
                UUID.randomUUID().toString(), null, createdPayload);
        orderEventProducer.publish(createdEvent);

        List<CartClient.CartItemResponse> reservedItems = new ArrayList<>();
        try {
            for (CartClient.CartItemResponse cartItem : cartResponse.items()) {
                log.info("INVENTORY RESERVE START - orderId={}, productId={}, quantity={}",
                        order.getId(), cartItem.productId(), cartItem.quantity());
                InventoryClient.InventoryResponse reserveResponse =
                        inventoryClient.reserveStock(cartItem.productId(),
                                cartItem.quantity(), authorizationHeader);
                reservedItems.add(cartItem);
                log.info("INVENTORY RESERVE SUCCESS - orderId={}, productId={}, quantity={}, availableStock={}, reservedStock={}",
                        order.getId(), cartItem.productId(), cartItem.quantity(),
                        reserveResponse == null ? null : reserveResponse.availableStock(),
                        reserveResponse == null ? null : reserveResponse.reservedStock());
            }
            log.info("CHECKOUT READY FOR PAYMENT - orderId={}, status={}, totalAmount={}",
                    order.getId(), order.getStatus(), order.getTotalAmount());
            return order;
        } catch (RuntimeException exception) {
            log.error("CHECKOUT RESERVATION FAILED - orderId={}, exceptionType={}, message={}",
                    order.getId(), exception.getClass().getName(), exception.getMessage(), exception);
            releaseReservedInventory(reservedItems, authorizationHeader);
            order.setStatus(OrderStatus.CANCELLED);
            order.setUpdatedAt(Instant.now());
            Order cancelledOrder = orderRepository.save(order);
            log.error("CHECKOUT CANCELLED AFTER RESERVATION FAILURE - orderId={}",
                    cancelledOrder.getId());
            return cancelledOrder;
        }
    }

    /** Backward-compatible overload for older tests and callers. */
    @Transactional
    public Order createOrderFromCart(String authorizationHeader, Long userId) {
        return createOrderFromCart(null, authorizationHeader, userId);
    }

    /** Processes the selected simulated payment for an authenticated CREATED order. */
    @Transactional
    public Order processOrderPayment(
            Long orderId,
            Long userId,
            String paymentMethod,
            String simulationMode,
            String authorizationHeader) {
        if (orderId == null || orderId <= 0) {
            throw new IllegalArgumentException("Order ID must be valid");
        }
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("User ID must be valid");
        }
        if (authorizationHeader == null || authorizationHeader.isBlank()) {
            throw new IllegalArgumentException(
                    "Authorization header must not be null or blank");
        }
        if (!List.of("UPI", "CARD", "DIGITAL_WALLET").contains(paymentMethod)) {
            throw new IllegalArgumentException("Unsupported payment method");
        }
        if (!List.of("SUCCESS", "FAILURE").contains(simulationMode)) {
            throw new IllegalArgumentException(
                    "Simulation mode must be SUCCESS or FAILURE");
        }

        Order order = orderRepository.findById(orderId)
                .filter(existing -> userId.equals(existing.getUserId()))
                .orElseThrow(() -> new IllegalArgumentException(
                        "Order not found for the authenticated user: " + orderId));
        if (order.getStatus() != OrderStatus.CREATED) {
            throw new IllegalStateException(
                    "Payment can only be processed for CREATED orders. Current status: "
                            + order.getStatus());
        }
        if (order.getItems() == null || order.getItems().isEmpty()) {
            throw new IllegalStateException("Order has no items: " + orderId);
        }

        String idempotencyKey = "MYSHOPYCART-ORDER-" + order.getId()
        + "-USER-" + order.getUserId();

        log.info("PAYMENT START - orderId={}, userId={}, amount={}, method={}, idempotencyKey={}",
                order.getId(), userId, order.getTotalAmount(), paymentMethod, idempotencyKey);

        PaymentClient.PaymentResponse paymentResponse = paymentClient.processPayment(
                order.getId(), userId, order.getTotalAmount(), idempotencyKey,
                paymentMethod, simulationMode, authorizationHeader);
        if (paymentResponse == null) {
            throw new IllegalStateException("Payment service returned an empty response");
        }
        log.info("PAYMENT RESPONSE - orderId={}, paymentId={}, status={}, method={}, gatewayReference={}, failureReason={}",
                order.getId(), paymentResponse.id(), paymentResponse.status(),
                paymentResponse.paymentMethod(), paymentResponse.gatewayReference(),
                paymentResponse.failureReason());

        if ("COMPLETED".equals(paymentResponse.status())) {
            // Use persisted order items, not the cart, for stock commitment.
            for (OrderItem item : order.getItems()) {
                log.info("INVENTORY COMMIT START - orderId={}, productId={}, quantity={}",
                        order.getId(), item.getProductId(), item.getQuantity());
                InventoryClient.InventoryResponse commitResponse =
                        inventoryClient.commitStock(item.getProductId(),
                                item.getQuantity(), authorizationHeader);
                log.info("INVENTORY COMMIT SUCCESS - orderId={}, productId={}, quantity={}, availableStock={}, reservedStock={}",
                        order.getId(), item.getProductId(), item.getQuantity(),
                        commitResponse == null ? null : commitResponse.availableStock(),
                        commitResponse == null ? null : commitResponse.reservedStock());
            }

            order.setStatus(OrderStatus.CONFIRMED);
            order.setUpdatedAt(Instant.now());
            Order confirmedOrder = orderRepository.save(order);
            InvoiceAddress checkoutAddress = InvoiceAddress.builder()
                    .address(confirmedOrder.getCustomerAddress())
                    .city(confirmedOrder.getCustomerCity())
                    .state(confirmedOrder.getCustomerState())
                    .postalCode(confirmedOrder.getCustomerPostalCode())
                    .country(confirmedOrder.getCustomerCountry())
                    .build();
            InvoiceCustomerSnapshot customerSnapshot = new InvoiceCustomerSnapshot(
                    confirmedOrder.getCustomerName(),
                    confirmedOrder.getCustomerEmail(),
                    confirmedOrder.getCustomerMobile(),
                    null,
                    checkoutAddress,
                    checkoutAddress);
            Invoice invoice = invoiceService.createInvoice(
                    confirmedOrder, customerSnapshot, paymentMethod);
            byte[] invoicePdf = invoicePdfGenerator.generate(invoice);
            minioStorageService.storeInvoicePdf(invoice.getInvoiceNumber(), invoicePdf);

            OrderConfirmedPayload confirmedPayload = new OrderConfirmedPayload(
                    confirmedOrder.getId(), confirmedOrder.getUserId(),
                    confirmedOrder.getCustomerName(), confirmedOrder.getCustomerEmail(),
                    confirmedOrder.getCustomerMobile(), confirmedOrder.getTotalAmount(),
                    invoice.getInvoiceNumber(), invoicePdf);
            MyShopyCartEvent<OrderConfirmedPayload> confirmedEvent = new MyShopyCartEvent<>(
                    UUID.randomUUID().toString(), "ORDER_CONFIRMED", 1, Instant.now(),
                    "order-service", "ORDER", String.valueOf(confirmedOrder.getId()),
                    UUID.randomUUID().toString(), null, confirmedPayload);
            orderEventProducer.publish(confirmedEvent);
            log.info("PAYMENT AND ORDER SUCCESS - orderId={}, status={}, invoiceNumber={}",
                    confirmedOrder.getId(), confirmedOrder.getStatus(), invoice.getInvoiceNumber());
            return confirmedOrder;
        }

        // Only a definitive payment failure reaches this compensation path.
        log.warn("PAYMENT FAILED - orderId={}, paymentStatus={}, releasing reserved inventory",
                order.getId(), paymentResponse.status());
        releaseReservedOrderItems(order.getItems(), authorizationHeader);
        order.setStatus(OrderStatus.CANCELLED);
        order.setUpdatedAt(Instant.now());
        Order cancelledOrder = orderRepository.save(order);
        log.warn("ORDER CANCELLED AFTER PAYMENT FAILURE - orderId={}", cancelledOrder.getId());
        return cancelledOrder;
    }

    private void releaseReservedInventory(
            List<CartClient.CartItemResponse> reservedItems,
            String authorizationHeader) {
        for (CartClient.CartItemResponse cartItem : reservedItems) {
            try {
                log.info("INVENTORY RELEASE START - productId={}, quantity={}",
                        cartItem.productId(), cartItem.quantity());
                InventoryClient.InventoryResponse releaseResponse = inventoryClient.releaseStock(
                        cartItem.productId(), cartItem.quantity(), authorizationHeader);
                log.info("INVENTORY RELEASE SUCCESS - productId={}, quantity={}, availableStock={}, reservedStock={}",
                        cartItem.productId(), cartItem.quantity(),
                        releaseResponse == null ? null : releaseResponse.availableStock(),
                        releaseResponse == null ? null : releaseResponse.reservedStock());
            } catch (RuntimeException compensationException) {
                log.error("INVENTORY RELEASE FAILED - productId={}, quantity={}, exceptionType={}, message={}",
                        cartItem.productId(), cartItem.quantity(),
                        compensationException.getClass().getName(),
                        compensationException.getMessage(), compensationException);
            }
        }
    }

    private void releaseReservedOrderItems(
            List<OrderItem> reservedItems,
            String authorizationHeader) {
        for (OrderItem item : reservedItems) {
            try {
                log.info("INVENTORY RELEASE START - productId={}, quantity={}",
                        item.getProductId(), item.getQuantity());
                InventoryClient.InventoryResponse releaseResponse = inventoryClient.releaseStock(
                        item.getProductId(), item.getQuantity(), authorizationHeader);
                log.info("INVENTORY RELEASE SUCCESS - productId={}, quantity={}, availableStock={}, reservedStock={}",
                        item.getProductId(), item.getQuantity(),
                        releaseResponse == null ? null : releaseResponse.availableStock(),
                        releaseResponse == null ? null : releaseResponse.reservedStock());
            } catch (RuntimeException compensationException) {
                log.error("INVENTORY RELEASE FAILED - productId={}, quantity={}, exceptionType={}, message={}",
                        item.getProductId(), item.getQuantity(),
                        compensationException.getClass().getName(),
                        compensationException.getMessage(), compensationException);
            }
        }
    }

    public Order updateStatus(Long orderId, OrderStatus newStatus) {
        if (newStatus == null) {
            throw new IllegalArgumentException("Order status must not be null");
        }
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderId));
        OrderStatus currentStatus = order.getStatus();
        if (currentStatus == null) {
            throw new IllegalStateException("Order status is not initialized");
        }
        if (!orderStatusTransition.isAllowed(currentStatus, newStatus)) {
            throw new IllegalStateException("Invalid order status transition from "
                    + currentStatus + " to " + newStatus);
        }
        order.setStatus(newStatus);
        order.setUpdatedAt(Instant.now());
        Order savedOrder = orderRepository.save(order);

        if (newStatus == OrderStatus.CANCELLED) {
            OrderCancelledPayload cancelledPayload = new OrderCancelledPayload(
                    savedOrder.getId(), savedOrder.getUserId(), savedOrder.getTotalAmount());
            MyShopyCartEvent<OrderCancelledPayload> cancelledEvent = new MyShopyCartEvent<>(
                    UUID.randomUUID().toString(), "ORDER_CANCELLED", 1, Instant.now(),
                    "order-service", "ORDER", String.valueOf(savedOrder.getId()),
                    UUID.randomUUID().toString(), null, cancelledPayload);
            orderEventProducer.publish(cancelledEvent);
        }
        return savedOrder;
    }

    private void validateQuantity(Integer quantity) {
        if (quantity == null || quantity <= 0) {
            throw new IllegalArgumentException(
                    "Order quantity must be greater than zero");
        }
    }
}