
package com.myshopycart.notificationservice.notification.status;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/notifications/status")
@RequiredArgsConstructor
public class NotificationStatusController {

    private final NotificationStatusService notificationStatusService;
    private final OrderOwnershipClient orderOwnershipClient;

    @GetMapping("/{messageId}")
    public ResponseEntity<NotificationStatusRecord> getByMessageId(
            @PathVariable String messageId,
            HttpServletRequest request) {

        final NotificationStatusRecord record;

        try {
            record = notificationStatusService.findByMessageId(messageId);
        } catch (IllegalArgumentException exception) {
            return ResponseEntity.notFound().build();
        }

        if (!orderOwnershipClient.isOrderAccessible(
                record.getOrderId(),
                request.getHeader("Authorization"))) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(record);
    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<List<NotificationStatusRecord>> getByOrderId(
            @PathVariable Long orderId,
            HttpServletRequest request) {

        if (!orderOwnershipClient.isOrderAccessible(
                orderId,
                request.getHeader("Authorization"))) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(
                notificationStatusService.findByOrderId(orderId));
    }
}