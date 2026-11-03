
package com.myshopycart.paymentservice.service;

import com.myshopycart.paymentservice.entity.Payment;
import com.myshopycart.paymentservice.entity.PaymentStatus;
import com.myshopycart.paymentservice.gateway.PaymentGateway;
import com.myshopycart.paymentservice.gateway.PaymentGatewayResult;
import com.myshopycart.paymentservice.messaging.event.MyShopyCartEvent;
import com.myshopycart.paymentservice.messaging.producer.PaymentEventProducer;
import com.myshopycart.paymentservice.repository.PaymentRepository;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.timelimiter.TimeLimiter;
import io.github.resilience4j.timelimiter.TimeLimiterConfig;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private PaymentGateway paymentGateway;

    @Mock
    private PaymentEventProducer paymentEventProducer;

    private PaymentService paymentService;

    @BeforeEach
    void setUp() {
        TimeLimiter timeLimiter = TimeLimiter.of(
                "paymentGatewayTest",
                TimeLimiterConfig.custom()
                        .timeoutDuration(Duration.ofSeconds(5))
                        .cancelRunningFuture(true)
                        .build());

        Retry retry = Retry.of(
                "paymentGatewayTest",
                RetryConfig.custom()
                        .maxAttempts(1)
                        .build());

        CircuitBreaker circuitBreaker = CircuitBreaker.of(
                "paymentGatewayTest",
                CircuitBreakerConfig.custom()
                        .slidingWindowSize(10)
                        .minimumNumberOfCalls(10)
                        .build());

        paymentService = new PaymentService(
                paymentRepository,
                paymentGateway,
                timeLimiter,
                retry,
                circuitBreaker,
                paymentEventProducer);
    }

    @Test
    void shouldCompleteSuccessfulPaymentAndPublishEvent() {
        Payment payment = createPayment();

        when(paymentRepository.findByIdempotencyKey("payment-key-1"))
                .thenReturn(Optional.empty());

	when(paymentRepository.save(any(Payment.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

        when(paymentGateway.processPayment(payment, "SUCCESS"))
                .thenReturn(PaymentGatewayResult.success("GW-REF-100"));

        Payment result = paymentService.processPayment(payment, "SUCCESS");

        assertEquals(PaymentStatus.COMPLETED, result.getStatus());
        assertEquals("GW-REF-100", result.getGatewayReference());
        assertEquals(null, result.getFailureReason());

        verify(paymentRepository, org.mockito.Mockito.times(2))
                .save(payment);
        verify(paymentGateway).processPayment(payment, "SUCCESS");
        verify(paymentEventProducer).publish(any(MyShopyCartEvent.class));
    }

    @Test
    void shouldReturnExistingPaymentForIdenticalIdempotentRequest() {
        Payment existing = createPayment();
        existing.setStatus(PaymentStatus.COMPLETED);
        existing.setGatewayReference("GW-REF-EXISTING");

        Payment repeatedRequest = createPayment();

        when(paymentRepository.findByIdempotencyKey("payment-key-1"))
                .thenReturn(Optional.of(existing));

        Payment result = paymentService.processPayment(
                repeatedRequest, "SUCCESS");

        assertSame(existing, result);
        assertEquals(PaymentStatus.COMPLETED, result.getStatus());

        verify(paymentGateway, never())
                .processPayment(any(Payment.class), any());
        verify(paymentRepository, never()).save(any(Payment.class));
        verify(paymentEventProducer, never())
                .publish(any(MyShopyCartEvent.class));
    }

    @Test
    void shouldRejectIdempotencyKeyReusedForDifferentAmount() {
        Payment existing = createPayment();
        Payment differentRequest = createPayment();
        differentRequest.setAmount(new BigDecimal("75.00"));

        when(paymentRepository.findByIdempotencyKey("payment-key-1"))
                .thenReturn(Optional.of(existing));

        assertThrows(
                IllegalStateException.class,
                () -> paymentService.processPayment(
                        differentRequest, "SUCCESS"));

        verify(paymentGateway, never())
                .processPayment(any(Payment.class), any());
        verify(paymentRepository, never()).save(any(Payment.class));
        verify(paymentEventProducer, never())
                .publish(any(MyShopyCartEvent.class));
    }

    @Test
    void shouldMarkPaymentFailedWhenGatewayDeclinesPayment() {
        Payment payment = createPayment();

        when(paymentRepository.findByIdempotencyKey("payment-key-1"))
                .thenReturn(Optional.empty());

	when(paymentRepository.save(any(Payment.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

        when(paymentGateway.processPayment(payment, "FAILURE"))
                .thenReturn(PaymentGatewayResult.failure("Payment declined"));

        Payment result = paymentService.processPayment(payment, "FAILURE");

        assertEquals(PaymentStatus.FAILED, result.getStatus());
        assertEquals(null, result.getGatewayReference());
        assertEquals("Payment declined", result.getFailureReason());

        verify(paymentRepository, org.mockito.Mockito.times(2))
                .save(payment);
        verify(paymentEventProducer).publish(any(MyShopyCartEvent.class));
    }

    @Test
    void shouldRejectPaymentWhenCircuitBreakerIsOpen() {
        Payment payment = createPayment();

        when(paymentRepository.findByIdempotencyKey("payment-key-1"))
                .thenReturn(Optional.empty());

	when(paymentRepository.save(any(Payment.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

        // Open the circuit before attempting payment.
        // The gateway should not be called.
        // This test uses the circuit breaker configured for this service.
        // A fresh service is constructed with an already-open breaker below.
        CircuitBreaker openCircuitBreaker = CircuitBreaker.of(
                "openPaymentGatewayTest",
                CircuitBreakerConfig.custom()
                        .slidingWindowSize(2)
                        .minimumNumberOfCalls(2)
                        .build());

        openCircuitBreaker.transitionToOpenState();

        paymentService = new PaymentService(
                paymentRepository,
                paymentGateway,
                TimeLimiter.ofDefaults("openPaymentGatewayTest"),
                Retry.ofDefaults("openPaymentGatewayTest"),
                openCircuitBreaker,
                paymentEventProducer);

        Payment result = paymentService.processPayment(payment, "SUCCESS");

        assertEquals(PaymentStatus.FAILED, result.getStatus());
        assertEquals(
                "Payment gateway circuit breaker is OPEN",
                result.getFailureReason());

        verify(paymentGateway, never())
                .processPayment(any(Payment.class), any());
        verify(paymentEventProducer).publish(any(MyShopyCartEvent.class));
    }

    @Test
    void shouldAllowPendingPaymentToTransitionToCompleted() {
        Payment payment = createPayment();
        payment.setId(100L);
        payment.setStatus(PaymentStatus.PENDING);

        when(paymentRepository.findById(100L))
                .thenReturn(Optional.of(payment));

	when(paymentRepository.save(any(Payment.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

        Payment result = paymentService.updateStatus(
                100L, PaymentStatus.COMPLETED);

        assertEquals(PaymentStatus.COMPLETED, result.getStatus());
        verify(paymentRepository).save(payment);
    }

    @Test
    void shouldAllowPendingPaymentToTransitionToFailed() {
        Payment payment = createPayment();
        payment.setId(100L);
        payment.setStatus(PaymentStatus.PENDING);

        when(paymentRepository.findById(100L))
                .thenReturn(Optional.of(payment));

	when(paymentRepository.save(any(Payment.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

        Payment result = paymentService.updateStatus(
                100L, PaymentStatus.FAILED);

        assertEquals(PaymentStatus.FAILED, result.getStatus());
        verify(paymentRepository).save(payment);
    }

    @Test
    void shouldRejectTransitionFromCompletedPayment() {
        Payment payment = createPayment();
        payment.setId(100L);
        payment.setStatus(PaymentStatus.COMPLETED);

        when(paymentRepository.findById(100L))
                .thenReturn(Optional.of(payment));

        assertThrows(
                IllegalStateException.class,
                () -> paymentService.updateStatus(
                        100L, PaymentStatus.FAILED));

        verify(paymentRepository, never()).save(any(Payment.class));
    }

    @Test
    void shouldRejectStatusUpdateForMissingPayment() {
        when(paymentRepository.findById(999L))
                .thenReturn(Optional.empty());

        assertThrows(
                IllegalArgumentException.class,
                () -> paymentService.updateStatus(
                        999L, PaymentStatus.COMPLETED));

        verify(paymentRepository, never()).save(any(Payment.class));
    }

    private Payment createPayment() {
        Payment payment = new Payment();
        payment.setId(1L);
        payment.setOrderId(200L);
        payment.setUserId(10L);
        payment.setAmount(new BigDecimal("25.00"));
        payment.setPaymentMethod("CARD");
        payment.setIdempotencyKey("payment-key-1");
        return payment;
    }
}
