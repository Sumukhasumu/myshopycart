package com.myshopycart.notificationservice.messaging.idempotency;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class IdempotencyService {

    private final ProcessedMessageRepository processedMessageRepository;

    public IdempotencyService(
            ProcessedMessageRepository processedMessageRepository) {

        this.processedMessageRepository = processedMessageRepository;
    }

    @Transactional(readOnly = true)
    public boolean alreadyProcessed(String messageId) {

        return processedMessageRepository.existsByMessageId(messageId);
    }

    @Transactional
    public void markProcessed(
            String messageId,
            String messageType,
            String source) {

        ProcessedMessage processedMessage =
                new ProcessedMessage(
                        messageId,
                        messageType,
                        source,
                        LocalDateTime.now()
                );

        processedMessageRepository.save(processedMessage);
    }
}