package com.myshopycart.notificationservice.sms;

public interface SmsProvider {

    void send(
            String recipientMobile,
            String message);
}