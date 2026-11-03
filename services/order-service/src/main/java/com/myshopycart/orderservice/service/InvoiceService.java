package com.myshopycart.orderservice.service;

import com.myshopycart.orderservice.entity.Invoice;
import com.myshopycart.orderservice.entity.InvoiceAddress;
import com.myshopycart.orderservice.entity.InvoiceItem;
import com.myshopycart.orderservice.entity.Order;
import com.myshopycart.orderservice.entity.OrderItem;
import com.myshopycart.orderservice.repository.InvoiceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class InvoiceService {

    private final InvoiceRepository invoiceRepository;

    private final InvoiceNumberGenerator invoiceNumberGenerator;

    @Transactional
    public Invoice createInvoice(
            Order order,
            InvoiceCustomerSnapshot customerSnapshot,
            String paymentMethod) {

        if (order == null) {
            throw new IllegalArgumentException(
                    "Order must not be null");
        }

        if (order.getId() == null) {
            throw new IllegalArgumentException(
                    "Order ID must not be null");
        }

        if (customerSnapshot == null) {
            throw new IllegalArgumentException(
                    "Customer snapshot must not be null");
        }

        if (customerSnapshot.customerName() == null ||
                customerSnapshot.customerName().isBlank()) {

            throw new IllegalArgumentException(
                    "Customer name must not be blank");
        }

        if (customerSnapshot.customerEmail() == null ||
                customerSnapshot.customerEmail().isBlank()) {

            throw new IllegalArgumentException(
                    "Customer email must not be blank");
        }

        if (invoiceRepository.existsByOrderId(
                order.getId())) {

            throw new IllegalStateException(
                    "Invoice already exists for order: "
                            + order.getId());
        }

        if (order.getItems() == null ||
                order.getItems().isEmpty()) {

            throw new IllegalArgumentException(
                    "Order must contain at least one item");
        }

        String invoiceNumber =
                invoiceNumberGenerator.generate();

        BigDecimal subtotal =
                calculateSubtotal(order);

        BigDecimal taxAmount =
                BigDecimal.ZERO;

        BigDecimal totalAmount =
                subtotal.add(taxAmount);

        Instant now = Instant.now();

        Invoice invoice =
                Invoice.builder()
                        .invoiceNumber(invoiceNumber)
                        .orderId(order.getId())
                        .issueDate(LocalDate.now())
                        .customerName(
                                customerSnapshot.customerName())
                        .customerEmail(
                                customerSnapshot.customerEmail())
                        .customerPhone(
                                customerSnapshot.customerPhone())
                        .customerGstin(
                                customerSnapshot.customerGstin())
                        .billingAddress(
                                customerSnapshot.billingAddress())
                        .shippingAddress(
                                customerSnapshot.shippingAddress())
                        .paymentMethod(paymentMethod)
                        .subtotal(subtotal)
                        .taxAmount(taxAmount)
                        .totalAmount(totalAmount)
                        .createdAt(now)
                        .updatedAt(now)
                        .build();

        for (OrderItem orderItem :
                order.getItems()) {

            BigDecimal lineTotal =
                    orderItem.getPrice()
                            .multiply(
                                    BigDecimal.valueOf(
                                            orderItem.getQuantity()));

            InvoiceItem invoiceItem =
                    InvoiceItem.builder()
                            .invoice(invoice)
                            .productId(
                                    orderItem.getProductId())
                            .productName(
                                    orderItem.getProductName())
                            .quantity(
                                    orderItem.getQuantity())
                            .unitPrice(
                                    orderItem.getPrice())
                            .lineTotal(lineTotal)
                            .build();

            invoice.getItems().add(
                    invoiceItem);
        }

        return invoiceRepository.save(invoice);
    }

    @Transactional(readOnly = true)
    public Invoice getInvoiceByOrderId(
            Long orderId) {

        if (orderId == null) {
            throw new IllegalArgumentException(
                    "Order ID must not be null");
        }

        return invoiceRepository
                .findByOrderId(orderId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Invoice not found for order: "
                                        + orderId));
    }

    @Transactional(readOnly = true)
    public Invoice getInvoiceByInvoiceNumber(
            String invoiceNumber) {

        if (invoiceNumber == null ||
                invoiceNumber.isBlank()) {

            throw new IllegalArgumentException(
                    "Invoice number must not be blank");
        }

        return invoiceRepository
                .findByInvoiceNumber(invoiceNumber)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Invoice not found: "
                                        + invoiceNumber));
    }

    private BigDecimal calculateSubtotal(
            Order order) {

        return order.getItems()
                .stream()
                .map(item ->
                        item.getPrice()
                                .multiply(
                                        BigDecimal.valueOf(
                                                item.getQuantity())))
                .reduce(
                        BigDecimal.ZERO,
                        BigDecimal::add);
    }
}