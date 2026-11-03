package com.myshopycart.notificationservice.notification.status;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface NotificationStatusRepository
        extends JpaRepository<NotificationStatusRecord, Long> {

    Optional<NotificationStatusRecord>
    findByMessageId(String messageId);

    List<NotificationStatusRecord>
    findByOrderId(Long orderId);

    List<NotificationStatusRecord>
    findByOrderIdAndChannel(
            Long orderId,
            NotificationChannel channel);
}