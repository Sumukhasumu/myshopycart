package com.myshopycart.paymentservice.service;

import com.myshopycart.paymentservice.entity.Payment;
import com.myshopycart.paymentservice.entity.PaymentStatus;
import com.myshopycart.paymentservice.gateway.PaymentGateway;
import com.myshopycart.paymentservice.gateway.PaymentGatewayException;
import com.myshopycart.paymentservice.gateway.PaymentGatewayResult;
import com.myshopycart.paymentservice.messaging.event.MyShopyCartEvent;
import com.myshopycart.paymentservice.messaging.event.PaymentEventPayload;
import com.myshopycart.paymentservice.messaging.producer.PaymentEventProducer;
import com.myshopycart.paymentservice.repository.PaymentRepository;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.timelimiter.TimeLimiter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeoutException;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentGateway paymentGateway;
    private final TimeLimiter paymentGatewayTimeLimiter;
    private final Retry paymentGatewayRetry;
    private final CircuitBreaker paymentGatewayCircuitBreaker;
    private final PaymentEventProducer paymentEventProducer;

    public PaymentService(
            PaymentRepository paymentRepository,
            PaymentGateway paymentGateway,
            TimeLimiter paymentGatewayTimeLimiter,
            Retry paymentGatewayRetry,
            CircuitBreaker paymentGatewayCircuitBreaker,
            PaymentEventProducer paymentEventProducer) {

        this.paymentRepository = paymentRepository;
        this.paymentGateway = paymentGateway;
        this.paymentGatewayTimeLimiter =
                paymentGatewayTimeLimiter;
        this.paymentGatewayRetry =
                paymentGatewayRetry;
        this.paymentGatewayCircuitBreaker =
                paymentGatewayCircuitBreaker;
        this.paymentEventProducer =
                paymentEventProducer;
    }

    @Transactional
    public Payment processPayment(
            Payment payment,
            String simulationMode) {

        Optional<Payment> existingPayment =
                paymentRepository.findByIdempotencyKey(
                        payment.getIdempotencyKey());

        if (existingPayment.isPresent()) {
	    Payment existing = existingPayment.get();

            boolean sameRequest =
            existing.getOrderId().equals(payment.getOrderId())
                    && existing.getUserId().equals(payment.getUserId())
                    && existing.getAmount().compareTo(payment.getAmount()) == 0
                    && existing.getPaymentMethod().equals(
                            payment.getPaymentMethod());

    if (!sameRequest) {
        throw new IllegalStateException(
                "Idempotency key was already used for a different payment request");
    }

    return existing;
}

        payment.setStatus(PaymentStatus.PENDING);

        Payment savedPayment =
                paymentRepository.save(payment);

        try {

            PaymentGatewayResult gatewayResult =
                    paymentGatewayCircuitBreaker.executeCheckedSupplier(
                            () ->
                                    paymentGatewayRetry
                                            .executeCheckedSupplier(
                                                    () -> {

                                                        PaymentGatewayResult result =
                                                                paymentGatewayTimeLimiter
                                                                        .executeFutureSupplier(
                                                                                () ->
                                                                                        CompletableFuture
                                                                                                .supplyAsync(
                                                                                                        () ->
                                                                                                                paymentGateway
                                                                                                                        .processPayment(
                                                                                                                                savedPayment,
                                                                                                                                simulationMode)
                                                                                                )
                                                                        );

                                                        if (!result.successful()) {
                                                            throw new PaymentGatewayException(
                                                                    result.failureReason());
                                                        }

                                                        return result;
                                                    }
                                            )
                    );

            savedPayment.setStatus(
                    PaymentStatus.COMPLETED);

            savedPayment.setGatewayReference(
                    gatewayResult.gatewayReference());

            savedPayment.setFailureReason(null);

        } catch (CallNotPermittedException ex) {

            savedPayment.setStatus(
                    PaymentStatus.FAILED);

            savedPayment.setGatewayReference(null);

            savedPayment.setFailureReason(
                    "Payment gateway circuit breaker is OPEN");

        } catch (PaymentGatewayException ex) {

            savedPayment.setStatus(
                    PaymentStatus.FAILED);

            savedPayment.setGatewayReference(null);

            savedPayment.setFailureReason(
                    ex.getMessage());

        } catch (TimeoutException ex) {

            savedPayment.setStatus(
                    PaymentStatus.FAILED);

            savedPayment.setGatewayReference(null);

            savedPayment.setFailureReason(
                    "Payment gateway timeout after retries");

        } catch (Throwable ex) {

            savedPayment.setStatus(
                    PaymentStatus.FAILED);

            savedPayment.setGatewayReference(null);

            savedPayment.setFailureReason(
                    "Payment gateway error: "
                            + ex.getMessage());
        }

        Payment finalPayment =
                paymentRepository.save(savedPayment);

        publishPaymentEvent(finalPayment);

        return finalPayment;
    }

    private void publishPaymentEvent(
            Payment payment) {

        PaymentEventPayload payload =
                new PaymentEventPayload(
                        payment.getId(),
                        payment.getOrderId(),
                        payment.getUserId(),
                        payment.getAmount(),
                        payment.getStatus().name(),
                        payment.getGatewayReference(),
                        payment.getFailureReason()
                );

        String eventType =
                payment.getStatus() == PaymentStatus.COMPLETED
                        ? "PAYMENT_COMPLETED"
                        : "PAYMENT_FAILED";

        MyShopyCartEvent<PaymentEventPayload> event =
                new MyShopyCartEvent<>(
                        UUID.randomUUID().toString(),
                        eventType,
                        1,
                        Instant.now(),
                        "payment-service",
                        "PAYMENT",
                        String.valueOf(payment.getId()),
                        payment.getIdempotencyKey(),
                        null,
                        payload
                );

        paymentEventProducer.publish(event);
    }

    @Transactional
    public Payment createPayment(Payment payment) {

        if (payment.getStatus() == null) {
            payment.setStatus(PaymentStatus.PENDING);
        }

        return paymentRepository.save(payment);
    }

    @Transactional(readOnly = true)
    public Optional<Payment> findById(Long paymentId) {

        return paymentRepository.findById(paymentId);
    }

    @Transactional(readOnly = true)
    public Optional<Payment> findByIdempotencyKey(
            String idempotencyKey) {

        return paymentRepository.findByIdempotencyKey(
                idempotencyKey);
    }

    @Transactional
    public Payment updateStatus(
            Long paymentId,
            PaymentStatus newStatus) {

        Payment payment =
                paymentRepository.findById(paymentId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Payment not found: "
                                                + paymentId));

        PaymentStatus currentStatus =
                payment.getStatus();

        if (!isValidTransition(
                currentStatus,
                newStatus)) {

            throw new IllegalStateException(
                    "Invalid payment status transition from "
                            + currentStatus
                            + " to "
                            + newStatus);
        }

        payment.setStatus(newStatus);

        return paymentRepository.save(payment);
    }

    private boolean isValidTransition(
            PaymentStatus currentStatus,
            PaymentStatus newStatus) {

        if (currentStatus == PaymentStatus.PENDING) {
            return newStatus == PaymentStatus.COMPLETED
                    || newStatus == PaymentStatus.FAILED;
        }

        return false;
    }
}