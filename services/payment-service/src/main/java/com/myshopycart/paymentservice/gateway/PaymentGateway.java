package com.myshopycart.paymentservice.gateway;

import com.myshopycart.paymentservice.entity.Payment;

public interface PaymentGateway {

    PaymentGatewayResult processPayment(
            Payment payment,
            String simulationMode
    );
}