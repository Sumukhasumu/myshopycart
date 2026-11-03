package com.myshopycart.orderservice.controller;

import com.myshopycart.orderservice.client.CartClient;
import com.myshopycart.orderservice.dto.CreateOrderRequest;
import com.myshopycart.orderservice.dto.UpdateOrderStatusRequest;
import com.myshopycart.orderservice.entity.Order;
import com.myshopycart.orderservice.service.InvoiceDownloadData;
import com.myshopycart.orderservice.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.myshopycart.orderservice.dto.ProcessOrderPaymentRequest;
import com.myshopycart.orderservice.dto.InvoiceDetailsResponse;
import com.myshopycart.orderservice.dto.CheckoutRequest;
import jakarta.validation.Valid;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<Order> createOrder(
            @RequestBody CreateOrderRequest request,
            @RequestHeader(HttpHeaders.AUTHORIZATION)
            String authorizationHeader,
            Authentication authentication) {

        Long userId =
                getAuthenticatedUserId(authentication);

        Order createdOrder =
                orderService.createOrder(
                        request,
                        userId,
                        authorizationHeader);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(createdOrder);
    }

    @GetMapping
    public ResponseEntity<List<Order>> getOrderHistory(
            Authentication authentication) {

        Long userId =
                getAuthenticatedUserId(authentication);

        return ResponseEntity.ok(
                orderService.findByUserId(userId));
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<Order> getOrderById(
            @PathVariable Long orderId,
            Authentication authentication) {

        Long userId =
                getAuthenticatedUserId(authentication);

        return orderService
                .findByIdForUser(orderId, userId)
                .map(ResponseEntity::ok)
                .orElseGet(() ->
                        ResponseEntity.notFound().build());
    }

    @GetMapping("/{orderId}/invoice/details")
    public ResponseEntity<InvoiceDetailsResponse> getInvoiceDetails(
            @PathVariable Long orderId,
            Authentication authentication) {

        Long userId = getAuthenticatedUserId(authentication);

        return orderService.getInvoiceDetailsForUser(orderId, userId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/{orderId}/invoice")
    public ResponseEntity<byte[]> downloadInvoice(
            @PathVariable Long orderId,
            Authentication authentication) {

        Long userId =
                getAuthenticatedUserId(authentication);

        return orderService
                .getInvoiceDownloadForUser(
                        orderId,
                        userId)
                .map(this::buildInvoiceResponse)
                .orElseGet(() ->
                        ResponseEntity.notFound().build());
    }

    @GetMapping("/checkout/cart")
    public ResponseEntity<CartClient.CartResponse> getCheckoutCart(
            @RequestHeader(HttpHeaders.AUTHORIZATION)
            String authorizationHeader) {

        CartClient.CartResponse cart =
                orderService.getCart(authorizationHeader);

        return ResponseEntity.ok(cart);
    }

    @PostMapping("/checkout")
    public ResponseEntity<Order> createOrderFromCart(
            @Valid @RequestBody CheckoutRequest request,
            @RequestHeader(HttpHeaders.AUTHORIZATION)
            String authorizationHeader,
            Authentication authentication) {

        Long userId = getAuthenticatedUserId(authentication);

        Order createdOrder = orderService.createOrderFromCart(
                request,
                authorizationHeader,
                userId);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(createdOrder);
    }

    @PostMapping("/{orderId}/payment")
    public ResponseEntity<Order> processOrderPayment(
            @PathVariable Long orderId,
            @Valid @RequestBody ProcessOrderPaymentRequest request,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader,
            Authentication authentication) {

        Long userId = getAuthenticatedUserId(authentication);

        Order processedOrder = orderService.processOrderPayment(
                orderId,
                userId,
                request.paymentMethod(),
                request.simulationMode(),
                authorizationHeader);

        return ResponseEntity.ok(processedOrder);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{orderId}/status")
    public ResponseEntity<Order> updateStatus(
            @PathVariable Long orderId,
            @RequestBody UpdateOrderStatusRequest request) {

        Order updatedOrder =
                orderService.updateStatus(
                        orderId,
                        request.status());

        return ResponseEntity.ok(updatedOrder);
    }

    private ResponseEntity<byte[]> buildInvoiceResponse(
            InvoiceDownloadData invoiceDownloadData) {

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .contentLength(
                        invoiceDownloadData.pdfBytes().length)
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\""
                                + invoiceDownloadData.invoiceNumber()
                                + ".pdf\"")
                .body(invoiceDownloadData.pdfBytes());
    }

    private Long getAuthenticatedUserId(
            Authentication authentication) {

        if (authentication == null ||
                authentication.getDetails() == null) {

            throw new IllegalStateException(
                    "Authenticated user ID not available");
        }

        return (Long) authentication.getDetails();
    }
}