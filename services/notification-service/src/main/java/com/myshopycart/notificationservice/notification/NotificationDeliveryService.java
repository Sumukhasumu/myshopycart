
package com.myshopycart.notificationservice.notification;

import com.myshopycart.notificationservice.email.EmailService;
import com.myshopycart.notificationservice.notification.status.NotificationChannel;
import com.myshopycart.notificationservice.notification.status.NotificationStatus;
import com.myshopycart.notificationservice.notification.status.NotificationStatusService;
import com.myshopycart.notificationservice.sms.SmsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NotificationDeliveryService {

    private final EmailService emailService;
    private final SmsService smsService;
    private final NotificationStatusService notificationStatusService;

    /**
     * Backward-compatible overload using a generated message ID.
     */
    public void sendOrderConfirmation(OrderNotificationData data) {
        sendOrderConfirmation(UUID.randomUUID().toString(), data);
    }

    /**
     * Sends order confirmations through enabled channels.
     * Email is enabled; SMS is temporarily disabled.
     */
    public void sendOrderConfirmation(
            String messageId,
            OrderNotificationData data) {

        validateMessageId(messageId);
        validateData(data);

        if (hasText(data.recipientEmail())) {
            sendEmail(messageId, data);
        }

        /*
         * SMS temporarily disabled because the paid MSG91 service
         * is not being used.
         *
         * if (hasText(data.recipientMobile())) {
         *     sendSms(messageId, data);
         * }
         */
    }

    private void sendEmail(
            String messageId,
            OrderNotificationData data) {

        String statusMessageId =
                buildChannelMessageId(messageId, NotificationChannel.EMAIL);

        if (!prepareChannel(
                statusMessageId,
                data.orderId(),
                NotificationChannel.EMAIL,
                data.recipientEmail())) {
            return;
        }

        try {
            emailService.sendOrderConfirmationEmail(
                    data.recipientEmail(),
                    data.customerName(),
                    data.orderId(),
                    data.totalAmount(),
                    data.invoiceNumber(),
                    data.invoicePdf());

            notificationStatusService.markSent(statusMessageId);

        } catch (RuntimeException e) {
            markFailedSafely(statusMessageId, e);
            throw e;
        }
    }

    /*
     * Retained for future SMS re-enablement.
     * This method is not called while SMS is disabled.
     */
    private void sendSms(
            String messageId,
            OrderNotificationData data) {

        String statusMessageId =
                buildChannelMessageId(messageId, NotificationChannel.SMS);

        if (!prepareChannel(
                statusMessageId,
                data.orderId(),
                NotificationChannel.SMS,
                data.recipientMobile())) {
            return;
        }

        try {
            smsService.sendOrderConfirmationSms(
                    data.recipientMobile(),
                    data.orderId(),
                    data.invoiceNumber(),
                    data.totalAmount().toPlainString());

            notificationStatusService.markSent(statusMessageId);

        } catch (RuntimeException e) {
            markFailedSafely(statusMessageId, e);
            throw e;
        }
    }

    private boolean prepareChannel(
            String statusMessageId,
            Long orderId,
            NotificationChannel channel,
            String recipient) {

        Optional<NotificationStatus> existing =
                notificationStatusService.findStatusIfPresent(
                        statusMessageId);

        if (existing.isPresent()) {
            NotificationStatus status = existing.get();

            if (status == NotificationStatus.SENT) {
                return false;
            }

            if (status == NotificationStatus.PROCESSING) {
                throw new IllegalStateException(
                        "Notification is already processing: "
                                + statusMessageId);
            }

            if (status == NotificationStatus.FAILED) {
                notificationStatusService.resetFailedForRetry(
                        statusMessageId);
            }
        } else {
            notificationStatusService.createPending(
                    statusMessageId,
                    orderId,
                    channel,
                    recipient);
        }

        notificationStatusService.markProcessing(statusMessageId);
        return true;
    }

    private void markFailedSafely(
            String statusMessageId,
            RuntimeException exception) {

        String reason = exception.getMessage();

        if (reason == null || reason.isBlank()) {
            reason = exception.getClass().getSimpleName();
        }

        try {
            notificationStatusService.markFailed(
                    statusMessageId,
                    reason);
        } catch (RuntimeException statusException) {
            System.err.println(
                    "Failed to record notification failure: "
                            + statusException.getMessage());
        }
    }

    private String buildChannelMessageId(
            String messageId,
            NotificationChannel channel) {

        return messageId + "-" + channel.name();
    }

    private void validateMessageId(String messageId) {
        if (messageId == null || messageId.isBlank()) {
            throw new IllegalArgumentException(
                    "Message ID must not be blank");
        }
    }

    private void validateData(OrderNotificationData data) {
        if (data == null) {
            throw new IllegalArgumentException(
                    "Order notification data must not be null");
        }

        if (!hasText(data.recipientEmail())
                && !hasText(data.recipientMobile())) {
            throw new IllegalArgumentException(
                    "At least one notification recipient must be available");
        }

        if (data.orderId() == null || data.orderId() <= 0) {
            throw new IllegalArgumentException(
                    "Order ID must be valid");
        }

        if (data.totalAmount() == null
                || data.totalAmount().signum() < 0) {
            throw new IllegalArgumentException(
                    "Total amount must be valid");
        }

        if (data.invoiceNumber() == null
                || data.invoiceNumber().isBlank()) {
            throw new IllegalArgumentException(
                    "Invoice number must not be blank");
        }
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}