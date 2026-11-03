package com.myshopycart.notificationservice.messaging.idempotency;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ProcessedMessageRepository
        extends JpaRepository<ProcessedMessage, String> {

    boolean existsByMessageId(String messageId);
}