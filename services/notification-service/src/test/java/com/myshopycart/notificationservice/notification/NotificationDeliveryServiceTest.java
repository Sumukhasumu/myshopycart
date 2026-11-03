
package com.myshopycart.notificationservice.notification;

import com.myshopycart.notificationservice.email.EmailService;
import com.myshopycart.notificationservice.notification.status.NotificationChannel;
import com.myshopycart.notificationservice.notification.status.NotificationStatusService;
import com.myshopycart.notificationservice.sms.SmsService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class NotificationDeliveryServiceTest {

    @Mock
    private EmailService emailService;

    @Mock
    private SmsService smsService;

    @Mock
    private NotificationStatusService notificationStatusService;

    @InjectMocks
    private NotificationDeliveryService notificationDeliveryService;

    @Test
    void shouldSendOrderConfirmationByEmailAndSkipSms() {
        OrderNotificationData data = new OrderNotificationData(
                "customer@example.com",
                "1234567890",
                "Test Customer",
                101L,
                new BigDecimal("2500.00"),
                "INV-2026-000101",
                new byte[]{1, 2, 3}
        );

        notificationDeliveryService.sendOrderConfirmation(
                "message-001", data);

        verify(notificationStatusService).createPending(
                "message-001-EMAIL",
                101L,
                NotificationChannel.EMAIL,
                "customer@example.com");

        verify(notificationStatusService).markProcessing(
                "message-001-EMAIL");

        verify(emailService).sendOrderConfirmationEmail(
                "customer@example.com",
                "Test Customer",
                101L,
                new BigDecimal("2500.00"),
                "INV-2026-000101",
                new byte[]{1, 2, 3});

        verify(notificationStatusService).markSent(
                "message-001-EMAIL");

        verify(smsService, never()).sendOrderConfirmationSms(
                anyString(),
                org.mockito.ArgumentMatchers.anyLong(),
                anyString(),
                anyString());

        verify(notificationStatusService, never()).createPending(
                "message-001-SMS",
                101L,
                NotificationChannel.SMS,
                "1234567890");
    }

    @Test
    void shouldRejectNullNotificationData() {
        assertThrows(
                IllegalArgumentException.class,
                () -> notificationDeliveryService.sendOrderConfirmation(
                        "message-001", null));

        verify(emailService, never()).sendOrderConfirmationEmail(
                anyString(),
                anyString(),
                org.mockito.ArgumentMatchers.anyLong(),
                org.mockito.ArgumentMatchers.any(),
                anyString(),
                org.mockito.ArgumentMatchers.any());

        verify(smsService, never()).sendOrderConfirmationSms(
                anyString(),
                org.mockito.ArgumentMatchers.anyLong(),
                anyString(),
                anyString());
    }

    @Test
    void shouldSkipSmsWhenEmailIsBlank() {
        OrderNotificationData data = new OrderNotificationData(
                "",
                "1234567890",
                "Test Customer",
                101L,
                new BigDecimal("2500.00"),
                "INV-2026-000101",
                null
        );

        assertDoesNotThrow(() ->
                notificationDeliveryService.sendOrderConfirmation(
                        "message-sms-only", data));

        verify(emailService, never()).sendOrderConfirmationEmail(
                anyString(),
                anyString(),
                org.mockito.ArgumentMatchers.anyLong(),
                org.mockito.ArgumentMatchers.any(),
                anyString(),
                org.mockito.ArgumentMatchers.any());

        verify(smsService, never()).sendOrderConfirmationSms(
                anyString(),
                org.mockito.ArgumentMatchers.anyLong(),
                anyString(),
                anyString());
    }

    @Test
    void shouldSendEmailWhenMobileIsBlank() {
        OrderNotificationData data = new OrderNotificationData(
                "customer@example.com",
                "",
                "Test Customer",
                101L,
                new BigDecimal("2500.00"),
                "INV-2026-000101",
                null
        );

        notificationDeliveryService.sendOrderConfirmation(
                "message-email-only", data);

        verify(emailService).sendOrderConfirmationEmail(
                "customer@example.com",
                "Test Customer",
                101L,
                new BigDecimal("2500.00"),
                "INV-2026-000101",
                null);

        verify(notificationStatusService).createPending(
                "message-email-only-EMAIL",
                101L,
                NotificationChannel.EMAIL,
                "customer@example.com");

        verify(notificationStatusService).markProcessing(
                "message-email-only-EMAIL");

        verify(notificationStatusService).markSent(
                "message-email-only-EMAIL");

        verify(smsService, never()).sendOrderConfirmationSms(
                anyString(),
                org.mockito.ArgumentMatchers.anyLong(),
                anyString(),
                anyString());
    }

    @Test
    void shouldRejectWhenBothRecipientsAreBlank() {
        OrderNotificationData data = new OrderNotificationData(
                "",
                "",
                "Test Customer",
                101L,
                new BigDecimal("2500.00"),
                "INV-2026-000101",
                null
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> notificationDeliveryService.sendOrderConfirmation(
                        "message-no-recipient", data));

        verify(emailService, never()).sendOrderConfirmationEmail(
                anyString(),
                anyString(),
                org.mockito.ArgumentMatchers.anyLong(),
                org.mockito.ArgumentMatchers.any(),
                anyString(),
                org.mockito.ArgumentMatchers.any());

        verify(smsService, never()).sendOrderConfirmationSms(
                anyString(),
                org.mockito.ArgumentMatchers.anyLong(),
                anyString(),
                anyString());
    }

    @Test
    void shouldRejectInvalidOrderId() {
        OrderNotificationData data = new OrderNotificationData(
                "customer@example.com",
                "1234567890",
                "Test Customer",
                0L,
                new BigDecimal("2500.00"),
                "INV-2026-000101",
                null
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> notificationDeliveryService.sendOrderConfirmation(
                        "message-001", data));
    }

    @Test
    void shouldRejectInvalidTotalAmount() {
        OrderNotificationData data = new OrderNotificationData(
                "customer@example.com",
                "1234567890",
                "Test Customer",
                101L,
                new BigDecimal("-1.00"),
                "INV-2026-000101",
                null
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> notificationDeliveryService.sendOrderConfirmation(
                        "message-001", data));
    }

    @Test
    void shouldRejectBlankInvoiceNumber() {
        OrderNotificationData data = new OrderNotificationData(
                "customer@example.com",
                "1234567890",
                "Test Customer",
                101L,
                new BigDecimal("2500.00"),
                "",
                null
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> notificationDeliveryService.sendOrderConfirmation(
                        "message-001", data));
    }

    @Test
    void shouldMarkEmailAsFailedWhenEmailDeliveryFails() {
        OrderNotificationData data = new OrderNotificationData(
                "customer@example.com",
                "1234567890",
                "Test Customer",
                101L,
                new BigDecimal("2500.00"),
                "INV-2026-000101",
                null
        );

        doThrow(new IllegalStateException("SMTP server unavailable"))
                .when(emailService)
                .sendOrderConfirmationEmail(
                        "customer@example.com",
                        "Test Customer",
                        101L,
                        new BigDecimal("2500.00"),
                        "INV-2026-000101",
                        null);

        assertThrows(
                IllegalStateException.class,
                () -> notificationDeliveryService.sendOrderConfirmation(
                        "message-002", data));

        verify(notificationStatusService).createPending(
                "message-002-EMAIL",
                101L,
                NotificationChannel.EMAIL,
                "customer@example.com");

        verify(notificationStatusService).markProcessing(
                "message-002-EMAIL");

        verify(notificationStatusService).markFailed(
                "message-002-EMAIL",
                "SMTP server unavailable");

        verify(smsService, never()).sendOrderConfirmationSms(
                anyString(),
                org.mockito.ArgumentMatchers.anyLong(),
                anyString(),
                anyString());
    }

    @Test
    void shouldNotAttemptSmsEvenWhenSmsProviderWouldFail() {
        OrderNotificationData data = new OrderNotificationData(
                "customer@example.com",
                "1234567890",
                "Test Customer",
                101L,
                new BigDecimal("2500.00"),
                "INV-2026-000101",
                null
        );

        assertDoesNotThrow(() ->
                notificationDeliveryService.sendOrderConfirmation(
                        "message-003", data));

        verify(emailService).sendOrderConfirmationEmail(
                "customer@example.com",
                "Test Customer",
                101L,
                new BigDecimal("2500.00"),
                "INV-2026-000101",
                null);

        verify(notificationStatusService).markSent(
                "message-003-EMAIL");

        verify(smsService, never()).sendOrderConfirmationSms(
                anyString(),
                org.mockito.ArgumentMatchers.anyLong(),
                anyString(),
                anyString());

        verify(notificationStatusService, never()).createPending(
                "message-003-SMS",
                101L,
                NotificationChannel.SMS,
                "1234567890");
    }
}
