package com.myshopycart.notificationservice.sms;

public interface SmsService {

    void sendOrderConfirmationSms(
            String recipientMobile,
            Long orderId,
            String invoiceNumber,
            String totalAmount);
}