package com.myshopycart.notificationservice.notification.status;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(
        name = "notification_status",
        indexes = {
                @Index(
                        name = "idx_notification_status_message_id",
                        columnList = "message_id"
                ),
                @Index(
                        name = "idx_notification_status_order_id",
                        columnList = "order_id"
                )
        }
)
public class NotificationStatusRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(
            name = "message_id",
            nullable = false,
            unique = true
    )
    private String messageId;

    @Column(
            name = "order_id",
            nullable = false
    )
    private Long orderId;

    @Enumerated(EnumType.STRING)
    @Column(
            nullable = false,
            length = 30
    )
    private NotificationChannel channel;

    @Enumerated(EnumType.STRING)
    @Column(
            nullable = false,
            length = 30
    )
    private NotificationStatus status;

    @Column(
            name = "recipient",
            nullable = false,
            length = 255
    )
    private String recipient;

    @Column(
            name = "failure_reason",
            length = 1000
    )
    private String failureReason;

    @Column(
            name = "created_at",
            nullable = false
    )
    private Instant createdAt;

    @Column(
            name = "updated_at",
            nullable = false
    )
    private Instant updatedAt;

    public NotificationStatusRecord() {
    }

    public Long getId() {
        return id;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public NotificationChannel getChannel() {
        return channel;
    }

    public void setChannel(NotificationChannel channel) {
        this.channel = channel;
    }

    public NotificationStatus getStatus() {
        return status;
    }

    public void setStatus(NotificationStatus status) {
        this.status = status;
    }

    public String getRecipient() {
        return recipient;
    }

    public void setRecipient(String recipient) {
        this.recipient = recipient;
    }

    public String getFailureReason() {
        return failureReason;
    }

    public void setFailureReason(String failureReason) {
        this.failureReason = failureReason;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}