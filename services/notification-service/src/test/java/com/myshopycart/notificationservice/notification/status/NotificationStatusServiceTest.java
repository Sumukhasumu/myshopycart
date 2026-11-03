
package com.myshopycart.notificationservice.notification.status;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationStatusServiceTest {

    @Mock
    private NotificationStatusRepository notificationStatusRepository;

    @InjectMocks
    private NotificationStatusService notificationStatusService;

    @Test
    void shouldCreatePendingNotification() {
        when(notificationStatusRepository.findByMessageId("message-001"))
                .thenReturn(Optional.empty());

        when(notificationStatusRepository.save(any(NotificationStatusRecord.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        NotificationStatusRecord result =
                notificationStatusService.createPending(
                        "message-001", 101L, NotificationChannel.SMS, "1234567890");

        assertEquals("message-001", result.getMessageId());
        assertEquals(101L, result.getOrderId());
        assertEquals(NotificationChannel.SMS, result.getChannel());
        assertEquals(NotificationStatus.PENDING, result.getStatus());
        assertEquals("1234567890", result.getRecipient());

        verify(notificationStatusRepository).save(any(NotificationStatusRecord.class));
    }

    @Test
    void shouldRejectDuplicateMessageId() {
        NotificationStatusRecord existing =
                createRecord("message-001", NotificationStatus.PENDING);

        when(notificationStatusRepository.findByMessageId("message-001"))
                .thenReturn(Optional.of(existing));

        assertThrows(
                IllegalStateException.class,
                () -> notificationStatusService.createPending(
                        "message-001", 101L, NotificationChannel.SMS, "1234567890"));

        verify(notificationStatusRepository, never())
                .save(any(NotificationStatusRecord.class));
    }

    @Test
    void shouldMovePendingToProcessing() {
        NotificationStatusRecord record =
                createRecord("message-001", NotificationStatus.PENDING);

        when(notificationStatusRepository.findByMessageId("message-001"))
                .thenReturn(Optional.of(record));

        when(notificationStatusRepository.save(any(NotificationStatusRecord.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        NotificationStatusRecord result =
                notificationStatusService.markProcessing("message-001");

        assertEquals(NotificationStatus.PROCESSING, result.getStatus());
        verify(notificationStatusRepository).save(record);
    }

    @Test
    void shouldMoveProcessingToSent() {
        NotificationStatusRecord record =
                createRecord("message-001", NotificationStatus.PROCESSING);
        record.setFailureReason("old failure");

        when(notificationStatusRepository.findByMessageId("message-001"))
                .thenReturn(Optional.of(record));

        when(notificationStatusRepository.save(any(NotificationStatusRecord.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        NotificationStatusRecord result =
                notificationStatusService.markSent("message-001");

        assertEquals(NotificationStatus.SENT, result.getStatus());
        assertEquals(null, result.getFailureReason());
        verify(notificationStatusRepository).save(record);
    }

    @Test
    void shouldMoveProcessingToFailed() {
        NotificationStatusRecord record =
                createRecord("message-001", NotificationStatus.PROCESSING);

        when(notificationStatusRepository.findByMessageId("message-001"))
                .thenReturn(Optional.of(record));

        when(notificationStatusRepository.save(any(NotificationStatusRecord.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        NotificationStatusRecord result =
                notificationStatusService.markFailed(
                        "message-001", "SMS provider unavailable");

        assertEquals(NotificationStatus.FAILED, result.getStatus());
        assertEquals("SMS provider unavailable", result.getFailureReason());
        verify(notificationStatusRepository).save(record);
    }

    @Test
    void shouldRejectInvalidPendingToSentTransition() {
        NotificationStatusRecord record =
                createRecord("message-001", NotificationStatus.PENDING);

        when(notificationStatusRepository.findByMessageId("message-001"))
                .thenReturn(Optional.of(record));

        assertThrows(
                IllegalStateException.class,
                () -> notificationStatusService.markSent("message-001"));

        verify(notificationStatusRepository, never())
                .save(any(NotificationStatusRecord.class));
    }

    @Test
    void shouldRejectInvalidSentToProcessingTransition() {
        NotificationStatusRecord record =
                createRecord("message-001", NotificationStatus.SENT);

        when(notificationStatusRepository.findByMessageId("message-001"))
                .thenReturn(Optional.of(record));

        assertThrows(
                IllegalStateException.class,
                () -> notificationStatusService.markProcessing("message-001"));

        verify(notificationStatusRepository, never())
                .save(any(NotificationStatusRecord.class));
    }

    @Test
    void shouldRejectInvalidFailedToSentTransition() {
        NotificationStatusRecord record =
                createRecord("message-001", NotificationStatus.FAILED);

        when(notificationStatusRepository.findByMessageId("message-001"))
                .thenReturn(Optional.of(record));

        assertThrows(
                IllegalStateException.class,
                () -> notificationStatusService.markSent("message-001"));

        verify(notificationStatusRepository, never())
                .save(any(NotificationStatusRecord.class));
    }

    @Test
    void shouldRejectBlankFailureReason() {
        assertThrows(
                IllegalArgumentException.class,
                () -> notificationStatusService.markFailed("message-001", ""));

        verify(notificationStatusRepository, never())
                .findByMessageId(any());

        verify(notificationStatusRepository, never())
                .save(any(NotificationStatusRecord.class));
    }

    @Test
    void shouldRejectMissingMessage() {
        when(notificationStatusRepository.findByMessageId("missing-message"))
                .thenReturn(Optional.empty());

        assertThrows(
                IllegalArgumentException.class,
                () -> notificationStatusService.findByMessageId("missing-message"));
    }

    private NotificationStatusRecord createRecord(
            String messageId,
            NotificationStatus status) {

        NotificationStatusRecord record = new NotificationStatusRecord();
        record.setMessageId(messageId);
        record.setOrderId(101L);
        record.setChannel(NotificationChannel.SMS);
        record.setStatus(status);
        record.setRecipient("1234567890");
        record.setCreatedAt(Instant.now());
        record.setUpdatedAt(Instant.now());

        return record;
    }
}