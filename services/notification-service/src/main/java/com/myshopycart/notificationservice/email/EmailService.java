package com.myshopycart.notificationservice.email;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    private final String fromAddress;

    public EmailService(
            JavaMailSender mailSender,
            @Value("${myshopycart.email.from}") String fromAddress) {

        this.mailSender = mailSender;
        this.fromAddress = fromAddress;
    }

    public void sendOrderConfirmationEmail(
            String recipientEmail,
            String customerName,
            Long orderId,
            BigDecimal totalAmount,
            String invoiceNumber,
            byte[] invoicePdf) {

        validateRecipientEmail(recipientEmail);
        validateOrderId(orderId);
        validateTotalAmount(totalAmount);
        validateInvoiceNumber(invoiceNumber);

        try {

            MimeMessage message =
                    mailSender.createMimeMessage();

            MimeMessageHelper helper =
                    new MimeMessageHelper(
                            message,
                            true,
                            "UTF-8");

            helper.setFrom(fromAddress);
            helper.setTo(recipientEmail);

            helper.setSubject(
                    "MYSHOPYCART Order Confirmation - Order #"
                            + orderId);

            helper.setText(
                    buildEmailBody(
                            customerName,
                            orderId,
                            totalAmount,
                            invoiceNumber),
                    false);

            if (invoicePdf != null &&
                    invoicePdf.length > 0) {

                helper.addAttachment(
                        invoiceNumber + ".pdf",
                        new ByteArrayResource(invoicePdf),
                        "application/pdf");
            }

            mailSender.send(message);

        } catch (MessagingException e) {

            throw new IllegalStateException(
                    "Failed to send order confirmation email",
                    e);
        }
    }

    private String buildEmailBody(
            String customerName,
            Long orderId,
            BigDecimal totalAmount,
            String invoiceNumber) {

        String displayName =
                customerName == null ||
                        customerName.isBlank()
                        ? "Customer"
                        : customerName;

        return """
                Dear %s,

                Thank you for your order with MYSHOPYCART.

                Your order has been confirmed successfully.

                Order ID: %d
                Invoice Number: %s
                Total Amount: %s

                Your invoice is attached to this email.

                Thank you for shopping with MYSHOPYCART.

                Regards,
                MYSHOPYCART Team
                """.formatted(
                displayName,
                orderId,
                invoiceNumber,
                totalAmount.toPlainString());
    }

    private void validateRecipientEmail(
            String recipientEmail) {

        if (recipientEmail == null ||
                recipientEmail.isBlank()) {

            throw new IllegalArgumentException(
                    "Recipient email must not be blank");
        }
    }

    private void validateOrderId(Long orderId) {

        if (orderId == null ||
                orderId <= 0) {

            throw new IllegalArgumentException(
                    "Order ID must be valid");
        }
    }

    private void validateTotalAmount(
            BigDecimal totalAmount) {

        if (totalAmount == null ||
                totalAmount.signum() < 0) {

            throw new IllegalArgumentException(
                    "Total amount must be valid");
        }
    }

    private void validateInvoiceNumber(
            String invoiceNumber) {

        if (invoiceNumber == null ||
                invoiceNumber.isBlank()) {

            throw new IllegalArgumentException(
                    "Invoice number must not be blank");
        }
    }
}