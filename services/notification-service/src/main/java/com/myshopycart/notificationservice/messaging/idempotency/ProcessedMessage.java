package com.myshopycart.notificationservice.messaging.idempotency;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "processed_messages")
public class ProcessedMessage {

    @Id
    @Column(name = "message_id", nullable = false, length = 100)
    private String messageId;

    @Column(name = "message_type", nullable = false, length = 100)
    private String messageType;

    @Column(name = "source", nullable = false, length = 100)
    private String source;

    @Column(name = "processed_at", nullable = false)
    private LocalDateTime processedAt;

    protected ProcessedMessage() {
    }

    public ProcessedMessage(
            String messageId,
            String messageType,
            String source,
            LocalDateTime processedAt) {

        this.messageId = messageId;
        this.messageType = messageType;
        this.source = source;
        this.processedAt = processedAt;
    }

    public String getMessageId() {
        return messageId;
    }

    public String getMessageType() {
        return messageType;
    }

    public String getSource() {
        return source;
    }

    public LocalDateTime getProcessedAt() {
        return processedAt;
    }
}