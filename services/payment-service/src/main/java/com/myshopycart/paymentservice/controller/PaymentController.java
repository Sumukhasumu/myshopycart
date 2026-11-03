
package com.myshopycart.paymentservice.controller;

import com.myshopycart.paymentservice.dto.PaymentRequest;
import com.myshopycart.paymentservice.dto.PaymentResponse;
import com.myshopycart.paymentservice.entity.Payment;
import com.myshopycart.paymentservice.service.PaymentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping
    public ResponseEntity<PaymentResponse> createPayment(
            @Valid @RequestBody PaymentRequest request) {

        Long authenticatedUserId = getAuthenticatedUserId();

        if (!authenticatedUserId.equals(request.userId())) {
            throw new AccessDeniedException(
                    "You cannot create a payment for another user");
        }

        Payment payment = new Payment();
        payment.setOrderId(request.orderId());
        payment.setUserId(authenticatedUserId);
        payment.setAmount(request.amount());
        payment.setIdempotencyKey(request.idempotencyKey());
        payment.setPaymentMethod(request.paymentMethod().name());

        Payment processedPayment =
                paymentService.processPayment(
                        payment, request.simulationMode());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(PaymentResponse.from(processedPayment));
    }

    @GetMapping("/{paymentId}")
    public ResponseEntity<PaymentResponse> getPayment(
            @PathVariable Long paymentId) {

        Long authenticatedUserId = getAuthenticatedUserId();

        return paymentService.findById(paymentId)
                .filter(payment ->
                        authenticatedUserId.equals(payment.getUserId()))
                .map(payment ->
                        ResponseEntity.ok(PaymentResponse.from(payment)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/by-idempotency-key/{idempotencyKey}")
    public ResponseEntity<PaymentResponse> getByIdempotencyKey(
            @PathVariable String idempotencyKey) {

        Long authenticatedUserId = getAuthenticatedUserId();

        return paymentService.findByIdempotencyKey(idempotencyKey)
                .filter(payment ->
                        authenticatedUserId.equals(payment.getUserId()))
                .map(payment ->
                        ResponseEntity.ok(PaymentResponse.from(payment)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    private Long getAuthenticatedUserId() {
    Authentication authentication =
            SecurityContextHolder.getContext().getAuthentication();

    if (authentication == null
            || !authentication.isAuthenticated()
            || !(authentication.getDetails() instanceof Long userId)
            || userId <= 0) {
        throw new AccessDeniedException(
                "Authenticated user identity could not be verified");
    }

    return userId;
}
}