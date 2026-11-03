package com.myshopycart.paymentservice.gateway;

public class PaymentGatewayException extends RuntimeException {

    public PaymentGatewayException(String message) {
        super(message);
    }
}