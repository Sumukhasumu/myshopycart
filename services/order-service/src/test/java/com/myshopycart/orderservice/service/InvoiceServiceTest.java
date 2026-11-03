package com.myshopycart.orderservice.service;

import com.myshopycart.orderservice.entity.Invoice;
import com.myshopycart.orderservice.entity.InvoiceAddress;
import com.myshopycart.orderservice.entity.Order;
import com.myshopycart.orderservice.entity.OrderItem;
import com.myshopycart.orderservice.entity.OrderStatus;
import com.myshopycart.orderservice.repository.InvoiceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InvoiceServiceTest {

    @Mock
    private InvoiceRepository invoiceRepository;

    @Mock
    private InvoiceNumberGenerator invoiceNumberGenerator;

    @InjectMocks
    private InvoiceService invoiceService;

    private Order order;

    @BeforeEach
    void setUp() {

        OrderItem firstItem =
                OrderItem.builder()
                        .productId("P100")
                        .productName("Laptop")
                        .quantity(2)
                        .price(new BigDecimal("500.00"))
                        .createdAt(Instant.now())
                        .build();

        OrderItem secondItem =
                OrderItem.builder()
                        .productId("P200")
                        .productName("Mouse")
                        .quantity(1)
                        .price(new BigDecimal("100.00"))
                        .createdAt(Instant.now())
                        .build();

        order =
                Order.builder()
                        .id(100L)
                        .userId(25L)
                        .status(OrderStatus.CONFIRMED)
                        .totalAmount(new BigDecimal("1100.00"))
                        .createdAt(Instant.now())
                        .updatedAt(Instant.now())
                        .items(new ArrayList<>())
                        .build();

        firstItem.setOrder(order);
        secondItem.setOrder(order);

        order.getItems().add(firstItem);
        order.getItems().add(secondItem);
    }

    @Test
    void shouldCreateInvoiceFromConfirmedOrder() {

        when(invoiceRepository.existsByOrderId(100L))
                .thenReturn(false);

        when(invoiceNumberGenerator.generate())
                .thenReturn("INV-2026-000001");

        when(invoiceRepository.save(any(Invoice.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0));

        InvoiceAddress billingAddress =
                InvoiceAddress.builder()
                        .address("Main Road")
                        .city("Kundapura")
                        .state("Karnataka")
                        .postalCode("576201")
                        .country("India")
                        .build();

        InvoiceAddress shippingAddress =
                InvoiceAddress.builder()
                        .address("Main Road")
                        .city("Kundapura")
                        .state("Karnataka")
                        .postalCode("576201")
                        .country("India")
                        .build();

        InvoiceCustomerSnapshot customerSnapshot =
                new InvoiceCustomerSnapshot(
                        "Test Customer",
                        "customer@example.com",
                        "9876543210",
                        null,
                        billingAddress,
                        shippingAddress);

        Invoice invoice =
                invoiceService.createInvoice(
                        order,
                        customerSnapshot,
                        "CARD");

        assertNotNull(invoice);

        assertEquals(
                "INV-2026-000001",
                invoice.getInvoiceNumber());

        assertEquals(
                100L,
                invoice.getOrderId());

        assertEquals(
                "Test Customer",
                invoice.getCustomerName());

        assertEquals(
                "customer@example.com",
                invoice.getCustomerEmail());

        assertEquals(
                "CARD",
                invoice.getPaymentMethod());

        assertEquals(
                new BigDecimal("1100.00"),
                invoice.getSubtotal());

        assertEquals(
                BigDecimal.ZERO,
                invoice.getTaxAmount());

        assertEquals(
                new BigDecimal("1100.00"),
                invoice.getTotalAmount());

        assertEquals(
                2,
                invoice.getItems().size());

        assertEquals(
                "Laptop",
                invoice.getItems()
                        .get(0)
                        .getProductName());

        assertEquals(
                new BigDecimal("1000.00"),
                invoice.getItems()
                        .get(0)
                        .getLineTotal());

        assertEquals(
                "Mouse",
                invoice.getItems()
                        .get(1)
                        .getProductName());

        assertEquals(
                new BigDecimal("100.00"),
                invoice.getItems()
                        .get(1)
                        .getLineTotal());

        verify(invoiceNumberGenerator)
                .generate();

        verify(invoiceRepository)
                .save(any(Invoice.class));
    }

    @Test
    void shouldRejectDuplicateInvoiceForOrder() {

        when(invoiceRepository.existsByOrderId(100L))
                .thenReturn(true);

        InvoiceCustomerSnapshot customerSnapshot =
                new InvoiceCustomerSnapshot(
                        "Test Customer",
                        "customer@example.com",
                        "9876543210",
                        null,
                        null,
                        null);

        assertThrows(
                IllegalStateException.class,
                () ->
                        invoiceService.createInvoice(
                                order,
                                customerSnapshot,
                                "CARD"));

        verify(invoiceNumberGenerator, never())
                .generate();

        verify(invoiceRepository, never())
                .save(any(Invoice.class));
    }

    @Test
    void shouldRejectOrderWithoutItems() {

        order.setItems(
                new ArrayList<>());

        when(invoiceRepository.existsByOrderId(100L))
                .thenReturn(false);

        InvoiceCustomerSnapshot customerSnapshot =
                new InvoiceCustomerSnapshot(
                        "Test Customer",
                        "customer@example.com",
                        "9876543210",
                        null,
                        null,
                        null);

        assertThrows(
                IllegalArgumentException.class,
                () ->
                        invoiceService.createInvoice(
                                order,
                                customerSnapshot,
                                "CARD"));

        verify(invoiceNumberGenerator, never())
                .generate();

        verify(invoiceRepository, never())
                .save(any(Invoice.class));
    }

    @Test
    void shouldRejectMissingCustomerName() {

        InvoiceCustomerSnapshot customerSnapshot =
                new InvoiceCustomerSnapshot(
                        "",
                        "customer@example.com",
                        "9876543210",
                        null,
                        null,
                        null);

        assertThrows(
                IllegalArgumentException.class,
                () ->
                        invoiceService.createInvoice(
                                order,
                                customerSnapshot,
                                "CARD"));

        verify(invoiceRepository, never())
                .existsByOrderId(
                        ArgumentMatchers.any(Long.class));

        verify(invoiceNumberGenerator, never())
                .generate();

        verify(invoiceRepository, never())
                .save(any(Invoice.class));
    }

    @Test
    void shouldRejectMissingCustomerEmail() {

        InvoiceCustomerSnapshot customerSnapshot =
                new InvoiceCustomerSnapshot(
                        "Test Customer",
                        "",
                        "9876543210",
                        null,
                        null,
                        null);

        assertThrows(
                IllegalArgumentException.class,
                () ->
                        invoiceService.createInvoice(
                                order,
                                customerSnapshot,
                                "CARD"));

        verify(invoiceRepository, never())
                .existsByOrderId(
                        ArgumentMatchers.any(Long.class));

        verify(invoiceNumberGenerator, never())
                .generate();

        verify(invoiceRepository, never())
                .save(any(Invoice.class));
    }

    @Test
    void shouldGetInvoiceByOrderId() {

        Invoice invoice =
                Invoice.builder()
                        .id(1L)
                        .invoiceNumber(
                                "INV-2026-000001")
                        .orderId(100L)
                        .customerName(
                                "Test Customer")
                        .customerEmail(
                                "customer@example.com")
                        .subtotal(
                                new BigDecimal("1100.00"))
                        .taxAmount(
                                BigDecimal.ZERO)
                        .totalAmount(
                                new BigDecimal("1100.00"))
                        .build();

        when(invoiceRepository.findByOrderId(100L))
                .thenReturn(Optional.of(invoice));

        Invoice result =
                invoiceService.getInvoiceByOrderId(100L);

        assertNotNull(result);

        assertEquals(
                "INV-2026-000001",
                result.getInvoiceNumber());

        assertEquals(
                100L,
                result.getOrderId());

        verify(invoiceRepository)
                .findByOrderId(100L);
    }

    @Test
    void shouldGetInvoiceByInvoiceNumber() {

        Invoice invoice =
                Invoice.builder()
                        .id(1L)
                        .invoiceNumber(
                                "INV-2026-000001")
                        .orderId(100L)
                        .customerName(
                                "Test Customer")
                        .customerEmail(
                                "customer@example.com")
                        .subtotal(
                                new BigDecimal("1100.00"))
                        .taxAmount(
                                BigDecimal.ZERO)
                        .totalAmount(
                                new BigDecimal("1100.00"))
                        .build();

        when(invoiceRepository
                .findByInvoiceNumber(
                        "INV-2026-000001"))
                .thenReturn(
                        Optional.of(invoice));

        Invoice result =
                invoiceService
                        .getInvoiceByInvoiceNumber(
                                "INV-2026-000001");

        assertNotNull(result);

        assertEquals(
                "INV-2026-000001",
                result.getInvoiceNumber());

        verify(invoiceRepository)
                .findByInvoiceNumber(
                        "INV-2026-000001");
    }

    @Test
    void shouldRejectMissingOrderId() {

        assertThrows(
                IllegalArgumentException.class,
                () ->
                        invoiceService
                                .getInvoiceByOrderId(null));

        verify(invoiceRepository, never())
                .findByOrderId(
                        ArgumentMatchers.any(Long.class));
    }

    @Test
    void shouldRejectMissingInvoiceNumber() {

        assertThrows(
                IllegalArgumentException.class,
                () ->
                        invoiceService
                                .getInvoiceByInvoiceNumber(""));

        verify(invoiceRepository, never())
                .findByInvoiceNumber(
                        ArgumentMatchers.any(String.class));
    }
}