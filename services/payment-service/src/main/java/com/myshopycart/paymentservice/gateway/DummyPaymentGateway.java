package com.myshopycart.paymentservice.gateway;

import com.myshopycart.paymentservice.entity.Payment;
import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.UUID;

@Component
public class DummyPaymentGateway implements PaymentGateway {

    @Override
    public PaymentGatewayResult processPayment(
            Payment payment,
            String simulationMode) {

        String mode = simulationMode == null
                ? "SUCCESS"
                : simulationMode.trim()
                        .toUpperCase(Locale.ROOT);

        switch (mode) {

            case "SUCCESS":
                return processSuccess();

            case "FAILURE":
                return processFailure();

            case "TIMEOUT":
                return processTimeout();

            default:
                return PaymentGatewayResult.failure(
                        "Unsupported simulation mode: " + mode
                );
        }
    }

    private PaymentGatewayResult processSuccess() {

        String gatewayReference =
                "DUMMY-" + UUID.randomUUID();

        return PaymentGatewayResult.success(
                gatewayReference
        );
    }

    private PaymentGatewayResult processFailure() {

        return PaymentGatewayResult.failure(
                "Dummy payment gateway simulated failure"
        );
    }

    private PaymentGatewayResult processTimeout() {

        try {
            Thread.sleep(5000);
        } catch (InterruptedException ex) {

            Thread.currentThread().interrupt();

            return PaymentGatewayResult.failure(
                    "Dummy payment gateway interrupted"
            );
        }

        return PaymentGatewayResult.success(
                "DUMMY-TIMEOUT-" + UUID.randomUUID()
        );
    }
}