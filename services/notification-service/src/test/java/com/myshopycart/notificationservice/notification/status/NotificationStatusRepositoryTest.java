package com.myshopycart.notificationservice.notification.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(
        replace = AutoConfigureTestDatabase.Replace.NONE
)
class NotificationStatusRepositoryTest {

    @Autowired
    private NotificationStatusRepository
            notificationStatusRepository;

    @Test
    void shouldSaveAndFindByMessageId() {

        NotificationStatusRecord record =
                createRecord(
                        "integration-message-001",
                        501L,
                        NotificationChannel.SMS,
                        "1234567890");

        NotificationStatusRecord saved =
                notificationStatusRepository.save(record);

        Optional<NotificationStatusRecord> result =
                notificationStatusRepository
                        .findByMessageId(
                                saved.getMessageId());

        assertTrue(result.isPresent());

        assertEquals(
                "integration-message-001",
                result.get().getMessageId());

        assertEquals(
                501L,
                result.get().getOrderId());

        assertEquals(
                NotificationChannel.SMS,
                result.get().getChannel());

        assertEquals(
                NotificationStatus.PENDING,
                result.get().getStatus());
    }

    @Test
    void shouldFindByOrderId() {

        NotificationStatusRecord emailRecord =
                createRecord(
                        "integration-message-002",
                        502L,
                        NotificationChannel.EMAIL,
                        "customer@example.com");

        NotificationStatusRecord smsRecord =
                createRecord(
                        "integration-message-003",
                        502L,
                        NotificationChannel.SMS,
                        "1234567890");

        notificationStatusRepository.save(
                emailRecord);

        notificationStatusRepository.save(
                smsRecord);

        List<NotificationStatusRecord> results =
                notificationStatusRepository
                        .findByOrderId(502L);

        assertEquals(
                2,
                results.size());
    }

    @Test
    void shouldFindByOrderIdAndChannel() {

        NotificationStatusRecord emailRecord =
                createRecord(
                        "integration-message-004",
                        503L,
                        NotificationChannel.EMAIL,
                        "customer@example.com");

        NotificationStatusRecord smsRecord =
                createRecord(
                        "integration-message-005",
                        503L,
                        NotificationChannel.SMS,
                        "1234567890");

        notificationStatusRepository.save(
                emailRecord);

        notificationStatusRepository.save(
                smsRecord);

        List<NotificationStatusRecord> results =
                notificationStatusRepository
                        .findByOrderIdAndChannel(
                                503L,
                                NotificationChannel.SMS);

        assertEquals(
                1,
                results.size());

        assertEquals(
                "integration-message-005",
                results.get(0).getMessageId());
    }

    private NotificationStatusRecord createRecord(
            String messageId,
            Long orderId,
            NotificationChannel channel,
            String recipient) {

        Instant now = Instant.now();

        NotificationStatusRecord record =
                new NotificationStatusRecord();

        record.setMessageId(messageId);
        record.setOrderId(orderId);
        record.setChannel(channel);
        record.setStatus(NotificationStatus.PENDING);
        record.setRecipient(recipient);
        record.setCreatedAt(now);
        record.setUpdatedAt(now);

        return record;
    }
}