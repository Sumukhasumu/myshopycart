package com.myshopycart.notificationservice.email;

import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mail.javamail.JavaMailSender;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class EmailServiceTest {

    private JavaMailSender mailSender;

    private EmailService emailService;

    private MimeMessage mimeMessage;

    @BeforeEach
    void setUp() {

        mailSender =
                mock(JavaMailSender.class);

        mimeMessage =
                new MimeMessage(
                        Session.getInstance(
                                new Properties()));

        when(mailSender.createMimeMessage())
                .thenReturn(mimeMessage);

        emailService =
                new EmailService(
                        mailSender,
                        "no-reply@myshopycart.local");
    }

    @Test
    void shouldSendOrderConfirmationEmail()
            throws Exception {

        byte[] pdf =
                "%PDF-1.4 MYSHOPYCART TEST"
                        .getBytes(
                                StandardCharsets.UTF_8);

        emailService.sendOrderConfirmationEmail(
                "customer@example.com",
                "Sumukha",
                25L,
                new BigDecimal("2600.00"),
                "INV-2026-000025",
                pdf);

        verify(mailSender)
                .createMimeMessage();

        verify(mailSender)
                .send(mimeMessage);

        assertEquals(
                "MYSHOPYCART Order Confirmation - Order #25",
                mimeMessage.getSubject());

        assertEquals(
                "no-reply@myshopycart.local",
                mimeMessage.getFrom()[0]
                        .toString());

        assertEquals(
                "customer@example.com",
                mimeMessage.getAllRecipients()[0]
                        .toString());
    }

    @Test
    void shouldSendEmailWithoutPdfAttachment()
            throws Exception {

        emailService.sendOrderConfirmationEmail(
                "customer@example.com",
                "Sumukha",
                25L,
                new BigDecimal("2600.00"),
                "INV-2026-000025",
                null);

        verify(mailSender)
                .send(mimeMessage);
    }

    @Test
    void shouldRejectBlankRecipientEmail() {

        assertThrows(
                IllegalArgumentException.class,
                () -> emailService
                        .sendOrderConfirmationEmail(
                                "",
                                "Sumukha",
                                25L,
                                new BigDecimal("2600.00"),
                                "INV-2026-000025",
                                null));

        verify(
                mailSender,
                never())
                .createMimeMessage();
    }

    @Test
    void shouldRejectInvalidOrderId() {

        assertThrows(
                IllegalArgumentException.class,
                () -> emailService
                        .sendOrderConfirmationEmail(
                                "customer@example.com",
                                "Sumukha",
                                0L,
                                new BigDecimal("2600.00"),
                                "INV-2026-000025",
                                null));

        verify(
                mailSender,
                never())
                .createMimeMessage();
    }

    @Test
    void shouldRejectInvalidAmount() {

        assertThrows(
                IllegalArgumentException.class,
                () -> emailService
                        .sendOrderConfirmationEmail(
                                "customer@example.com",
                                "Sumukha",
                                25L,
                                new BigDecimal("-1.00"),
                                "INV-2026-000025",
                                null));

        verify(
                mailSender,
                never())
                .createMimeMessage();
    }

    @Test
    void shouldRejectBlankInvoiceNumber() {

        assertThrows(
                IllegalArgumentException.class,
                () -> emailService
                        .sendOrderConfirmationEmail(
                                "customer@example.com",
                                "Sumukha",
                                25L,
                                new BigDecimal("2600.00"),
                                "",
                                null));

        verify(
                mailSender,
                never())
                .createMimeMessage();
    }
}