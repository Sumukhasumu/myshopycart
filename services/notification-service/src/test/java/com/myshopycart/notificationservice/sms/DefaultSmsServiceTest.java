package com.myshopycart.notificationservice.sms;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class DefaultSmsServiceTest {

    private SmsProvider smsProvider;
    private DefaultSmsService smsService;

    @BeforeEach
    void setUp() {

        smsProvider = mock(SmsProvider.class);

        smsService =
                new DefaultSmsService(smsProvider);
    }

    @Test
    void shouldSendOrderConfirmationSms() {

        smsService.sendOrderConfirmationSms(
                "+911234567890",
                25L,
                "INV-2026-000025",
                "2600.00");

        verify(smsProvider).send(
                eq("+911234567890"),
                contains("Order #25"));

        verify(smsProvider).send(
                eq("+911234567890"),
                contains("INV-2026-000025"));

        verify(smsProvider).send(
                eq("+911234567890"),
                contains("2600.00"));
    }

    @Test
    void shouldRejectBlankMobileNumber() {

        assertThrows(
                IllegalArgumentException.class,
                () -> smsService
                        .sendOrderConfirmationSms(
                                "",
                                25L,
                                "INV-2026-000025",
                                "2600.00"));

        verifyNoInteractions(smsProvider);
    }

    @Test
    void shouldRejectInvalidOrderId() {

        assertThrows(
                IllegalArgumentException.class,
                () -> smsService
                        .sendOrderConfirmationSms(
                                "+911234567890",
                                0L,
                                "INV-2026-000025",
                                "2600.00"));

        verifyNoInteractions(smsProvider);
    }

    @Test
    void shouldRejectBlankInvoiceNumber() {

        assertThrows(
                IllegalArgumentException.class,
                () -> smsService
                        .sendOrderConfirmationSms(
                                "+911234567890",
                                25L,
                                "",
                                "2600.00"));

        verifyNoInteractions(smsProvider);
    }

    @Test
    void shouldRejectBlankTotalAmount() {

        assertThrows(
                IllegalArgumentException.class,
                () -> smsService
                        .sendOrderConfirmationSms(
                                "+911234567890",
                                25L,
                                "INV-2026-000025",
                                ""));

        verifyNoInteractions(smsProvider);
    }
}