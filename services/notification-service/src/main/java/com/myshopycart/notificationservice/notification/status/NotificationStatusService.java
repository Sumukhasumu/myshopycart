
package com.myshopycart.notificationservice.notification.status;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class NotificationStatusService {

    private final NotificationStatusRepository repository;

    @Transactional
    public NotificationStatusRecord createPending(
            String messageId,
            Long orderId,
            NotificationChannel channel,
            String recipient) {

        validateMessageId(messageId);

        if (orderId == null || orderId <= 0) {
            throw new IllegalArgumentException("Order ID must be valid");
        }
        if (channel == null) {
            throw new IllegalArgumentException(
                    "Notification channel must not be null");
        }
        if (recipient == null || recipient.isBlank()) {
            throw new IllegalArgumentException(
                    "Recipient must not be blank");
        }
        if (repository.findByMessageId(messageId).isPresent()) {
            throw new IllegalStateException(
                    "Notification status already exists for message: "
                            + messageId);
        }

        Instant now = Instant.now();
        NotificationStatusRecord record = new NotificationStatusRecord();
        record.setMessageId(messageId);
        record.setOrderId(orderId);
        record.setChannel(channel);
        record.setStatus(NotificationStatus.PENDING);
        record.setRecipient(recipient);
        record.setCreatedAt(now);
        record.setUpdatedAt(now);

        return repository.save(record);
    }

    @Transactional(readOnly = true)
    public Optional<NotificationStatus> findStatusIfPresent(
            String messageId) {
        validateMessageId(messageId);
        return repository.findByMessageId(messageId)
                .map(NotificationStatusRecord::getStatus);
    }

    @Transactional
    public NotificationStatusRecord resetFailedForRetry(String messageId) {
        NotificationStatusRecord record = findRecord(messageId);

        if (record.getStatus() != NotificationStatus.FAILED) {
            throw new IllegalStateException(
                    "Only FAILED notifications can be retried: " + messageId);
        }

        record.setStatus(NotificationStatus.PENDING);
        record.setFailureReason(null);
        record.setUpdatedAt(Instant.now());
        return repository.save(record);
    }

    @Transactional
    public NotificationStatusRecord markProcessing(String messageId) {
        NotificationStatusRecord record = findRecord(messageId);

        if (record.getStatus() != NotificationStatus.PENDING) {
            throw new IllegalStateException(
                    "Notification cannot transition to PROCESSING from "
                            + record.getStatus() + ": " + messageId);
        }

        record.setStatus(NotificationStatus.PROCESSING);
        record.setUpdatedAt(Instant.now());
        return repository.save(record);
    }

    @Transactional
    public NotificationStatusRecord markSent(String messageId) {
        NotificationStatusRecord record = findRecord(messageId);

        if (record.getStatus() != NotificationStatus.PROCESSING) {
            throw new IllegalStateException(
                    "Notification cannot transition to SENT from "
                            + record.getStatus() + ": " + messageId);
        }

        record.setStatus(NotificationStatus.SENT);
        record.setUpdatedAt(Instant.now());
        record.setFailureReason(null);
        return repository.save(record);
    }

    @Transactional
    public NotificationStatusRecord markFailed(
            String messageId,
            String reason) {

        if (reason == null || reason.isBlank()) {
            throw new IllegalArgumentException(
                    "Failure reason must not be blank");
        }

        NotificationStatusRecord record = findRecord(messageId);

        if (record.getStatus() != NotificationStatus.PROCESSING) {
            throw new IllegalStateException(
                    "Notification cannot transition to FAILED from "
                            + record.getStatus() + ": " + messageId);
        }

        record.setStatus(NotificationStatus.FAILED);
        record.setUpdatedAt(Instant.now());
        record.setFailureReason(reason);
        return repository.save(record);
    }

    @Transactional(readOnly = true)
    public NotificationStatusRecord findByMessageId(String messageId) {
        return findRecord(messageId);
    }

    @Transactional(readOnly = true)
    public List<NotificationStatusRecord> findByOrderId(Long orderId) {
        if (orderId == null || orderId <= 0) {
            throw new IllegalArgumentException("Order ID must be valid");
        }
        return repository.findByOrderId(orderId);
    }

    private NotificationStatusRecord findRecord(String messageId) {
        validateMessageId(messageId);
        return repository.findByMessageId(messageId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Notification status not found for message: "
                                + messageId));
    }

    private void validateMessageId(String messageId) {
        if (messageId == null || messageId.isBlank()) {
            throw new IllegalArgumentException(
                    "Message ID must not be blank");
        }
    }
}
