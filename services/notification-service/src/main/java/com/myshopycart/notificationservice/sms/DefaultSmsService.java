package com.myshopycart.notificationservice.sms;

import org.springframework.stereotype.Service;

@Service
public class DefaultSmsService implements SmsService {

    private final SmsProvider smsProvider;

    public DefaultSmsService(SmsProvider smsProvider) {
        this.smsProvider = smsProvider;
    }

    @Override
    public void sendOrderConfirmationSms(
            String recipientMobile,
            Long orderId,
            String invoiceNumber,
            String totalAmount) {

        validateRecipientMobile(recipientMobile);
        validateOrderId(orderId);
        validateInvoiceNumber(invoiceNumber);
        validateTotalAmount(totalAmount);

        String message = buildOrderConfirmationMessage(
                orderId,
                invoiceNumber,
                totalAmount);

        smsProvider.send(
                recipientMobile,
                message);
    }

    private String buildOrderConfirmationMessage(
            Long orderId,
            String invoiceNumber,
            String totalAmount) {

        return """
                MYSHOPYCART: Order #%d confirmed. \
                Invoice: %s. Total: %s. \
                Thank you for shopping with MYSHOPYCART.
                """.formatted(
                orderId,
                invoiceNumber,
                totalAmount);
    }

    private void validateRecipientMobile(
            String recipientMobile) {

        if (recipientMobile == null
                || recipientMobile.isBlank()) {

            throw new IllegalArgumentException(
                    "Recipient mobile number must not be blank");
        }
    }

    private void validateOrderId(Long orderId) {

        if (orderId == null || orderId <= 0) {

            throw new IllegalArgumentException(
                    "Order ID must be valid");
        }
    }

    private void validateInvoiceNumber(
            String invoiceNumber) {

        if (invoiceNumber == null
                || invoiceNumber.isBlank()) {

            throw new IllegalArgumentException(
                    "Invoice number must not be blank");
        }
    }

    private void validateTotalAmount(
            String totalAmount) {

        if (totalAmount == null
                || totalAmount.isBlank()) {

            throw new IllegalArgumentException(
                    "Total amount must not be blank");
        }
    }
}