
package com.myshopycart.notificationservice.notification;

import com.myshopycart.notificationservice.email.EmailService;
import com.myshopycart.notificationservice.notification.status.NotificationChannel;
import com.myshopycart.notificationservice.notification.status.NotificationStatus;
import com.myshopycart.notificationservice.notification.status.NotificationStatusService;
import com.myshopycart.notificationservice.sms.SmsService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationDeliveryRetryTest {

    @Mock
    private EmailService emailService;

    @Mock
    private SmsService smsService;

    @Mock
    private NotificationStatusService notificationStatusService;

    @InjectMocks
    private NotificationDeliveryService notificationDeliveryService;

    @Test
    void shouldNotResendEmailWhenAlreadySent() {
        OrderNotificationData data = emailOnlyData();

        when(notificationStatusService.findStatusIfPresent(
                "message-sent-EMAIL"))
                .thenReturn(Optional.of(NotificationStatus.SENT));

        notificationDeliveryService.sendOrderConfirmation(
                "message-sent", data);

        verify(emailService, never()).sendOrderConfirmationEmail(
                any(), any(), any(), any(), any(), any());

        verify(notificationStatusService, never())
                .createPending(
                        "message-sent-EMAIL",
                        101L,
                        NotificationChannel.EMAIL,
                        "customer@example.com");

        verify(notificationStatusService, never())
                .markProcessing("message-sent-EMAIL");

        verify(notificationStatusService, never())
                .resetFailedForRetry("message-sent-EMAIL");
    }

    @Test
    void shouldRetryEmailWhenPreviousAttemptFailed() {
        OrderNotificationData data = emailOnlyData();

        when(notificationStatusService.findStatusIfPresent(
                "message-failed-EMAIL"))
                .thenReturn(Optional.of(NotificationStatus.FAILED));

        notificationDeliveryService.sendOrderConfirmation(
                "message-failed", data);

        verify(notificationStatusService)
                .resetFailedForRetry("message-failed-EMAIL");

        verify(notificationStatusService)
                .markProcessing("message-failed-EMAIL");

        verify(emailService).sendOrderConfirmationEmail(
                "customer@example.com",
                "Test Customer",
                101L,
                new BigDecimal("2500.00"),
                "INV-2026-000101",
                null);

        verify(notificationStatusService)
                .markSent("message-failed-EMAIL");

        verify(notificationStatusService, never())
                .createPending(
                        "message-failed-EMAIL",
                        101L,
                        NotificationChannel.EMAIL,
                        "customer@example.com");

        verify(smsService, never()).sendOrderConfirmationSms(
                any(), any(), any(), any());
    }

    private OrderNotificationData emailOnlyData() {
        return new OrderNotificationData(
                "customer@example.com",
                "",
                "Test Customer",
                101L,
                new BigDecimal("2500.00"),
                "INV-2026-000101",
                null);
    }
}