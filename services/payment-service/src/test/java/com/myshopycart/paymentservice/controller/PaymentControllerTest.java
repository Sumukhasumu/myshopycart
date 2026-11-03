
package com.myshopycart.paymentservice.controller;

import com.myshopycart.paymentservice.dto.PaymentRequest;
import com.myshopycart.paymentservice.entity.Payment;
import com.myshopycart.paymentservice.entity.PaymentMethod;
import com.myshopycart.paymentservice.service.PaymentService;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentControllerTest {

    @Mock
    private PaymentService paymentService;

    @InjectMocks
    private PaymentController paymentController;

    private Payment payment;

    @BeforeEach
    void setUp() {
        payment = new Payment();
        payment.setId(100L);
        payment.setOrderId(200L);
        payment.setUserId(10L);
        payment.setAmount(new BigDecimal("25.00"));
        payment.setPaymentMethod("CARD");
        payment.setIdempotencyKey("test-key-100");

        setAuthenticatedUser(10L);
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldRejectPaymentCreationForAnotherUser() {
        PaymentRequest request = new PaymentRequest(
                200L,
                20L,
                new BigDecimal("25.00"),
                "test-key-100",
                PaymentMethod.CARD,
                null);

        assertThrows(
                AccessDeniedException.class,
                () -> paymentController.createPayment(request));

        verify(paymentService, never())
                .processPayment(any(Payment.class), nullable(String.class));
    }

    @Test
    void shouldReturnNotFoundWhenPaymentBelongsToAnotherUser() {
        setAuthenticatedUser(20L);

        when(paymentService.findById(100L))
                .thenReturn(Optional.of(payment));

        ResponseEntity<?> response = paymentController.getPayment(100L);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void shouldReturnNotFoundForAnotherUsersIdempotencyKey() {
        setAuthenticatedUser(20L);

        when(paymentService.findByIdempotencyKey("test-key-100"))
                .thenReturn(Optional.of(payment));

        ResponseEntity<?> response =
                paymentController.getByIdempotencyKey("test-key-100");

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void shouldReturnNotFoundWhenPaymentDoesNotExist() {
        when(paymentService.findById(999L))
                .thenReturn(Optional.empty());

        ResponseEntity<?> response = paymentController.getPayment(999L);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void shouldAllowUserToReadTheirOwnPayment() {
        when(paymentService.findById(100L))
                .thenReturn(Optional.of(payment));

        ResponseEntity<?> response = paymentController.getPayment(100L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void shouldAllowUserToReadTheirOwnIdempotencyKey() {
        when(paymentService.findByIdempotencyKey("test-key-100"))
                .thenReturn(Optional.of(payment));

        ResponseEntity<?> response =
                paymentController.getByIdempotencyKey("test-key-100");

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void shouldRejectRequestWhenUserIdentityIsMissing() {
        SecurityContextHolder.clearContext();

        assertThrows(
                AccessDeniedException.class,
                () -> paymentController.getPayment(100L));

        verify(paymentService, never()).findById(100L);
    }

    private void setAuthenticatedUser(Long userId) {
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        "test-user",
                        null,
                        List.of(new SimpleGrantedAuthority("ROLE_USER")));

        authentication.setDetails(userId);

        SecurityContextHolder.getContext()
                .setAuthentication(authentication);
    }
}