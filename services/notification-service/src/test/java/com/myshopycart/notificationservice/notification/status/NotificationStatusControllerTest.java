
package com.myshopycart.notificationservice.notification.status;

import jakarta.servlet.http.HttpServletRequest;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationStatusControllerTest {

    @Mock
    private NotificationStatusService notificationStatusService;

    @Mock
    private OrderOwnershipClient orderOwnershipClient;

    @Mock
    private HttpServletRequest request;

    @InjectMocks
    private NotificationStatusController notificationStatusController;

    private static final String MESSAGE_ID = "message-test-1";
    private static final Long ORDER_ID = 100L;
    private static final String AUTHORIZATION = "Bearer test-token";

    @Test
    void getByMessageIdReturnsRecordWhenOrderIsAccessible() {
        NotificationStatusRecord record =
                mock(NotificationStatusRecord.class);

        when(request.getHeader("Authorization"))
                .thenReturn(AUTHORIZATION);
        when(notificationStatusService.findByMessageId(MESSAGE_ID))
                .thenReturn(record);
        when(record.getOrderId()).thenReturn(ORDER_ID);
        when(orderOwnershipClient.isOrderAccessible(
                ORDER_ID, AUTHORIZATION))
                .thenReturn(true);

        ResponseEntity<NotificationStatusRecord> response =
                notificationStatusController.getByMessageId(
                        MESSAGE_ID, request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(record, response.getBody());
        verify(orderOwnershipClient)
                .isOrderAccessible(ORDER_ID, AUTHORIZATION);
    }

    @Test
    void getByMessageIdReturnsNotFoundWhenRecordDoesNotExist() {
        when(notificationStatusService.findByMessageId(MESSAGE_ID))
                .thenThrow(new IllegalArgumentException(
                        "Notification not found"));

        ResponseEntity<NotificationStatusRecord> response =
                notificationStatusController.getByMessageId(
                        MESSAGE_ID, request);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verifyNoInteractions(orderOwnershipClient);
    }

    @Test
    void getByMessageIdReturnsNotFoundWhenOrderIsNotAccessible() {
        NotificationStatusRecord record =
                mock(NotificationStatusRecord.class);

        when(request.getHeader("Authorization"))
                .thenReturn(AUTHORIZATION);
        when(notificationStatusService.findByMessageId(MESSAGE_ID))
                .thenReturn(record);
        when(record.getOrderId()).thenReturn(ORDER_ID);
        when(orderOwnershipClient.isOrderAccessible(
                ORDER_ID, AUTHORIZATION))
                .thenReturn(false);

        ResponseEntity<NotificationStatusRecord> response =
                notificationStatusController.getByMessageId(
                        MESSAGE_ID, request);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(orderOwnershipClient)
                .isOrderAccessible(ORDER_ID, AUTHORIZATION);
    }

    @Test
    void getByOrderIdReturnsRecordsWhenOrderIsAccessible() {
        List<NotificationStatusRecord> records =
                List.of(mock(NotificationStatusRecord.class));

        when(request.getHeader("Authorization"))
                .thenReturn(AUTHORIZATION);
        when(orderOwnershipClient.isOrderAccessible(
                ORDER_ID, AUTHORIZATION))
                .thenReturn(true);
        when(notificationStatusService.findByOrderId(ORDER_ID))
                .thenReturn(records);

        ResponseEntity<List<NotificationStatusRecord>> response =
                notificationStatusController.getByOrderId(
                        ORDER_ID, request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(records, response.getBody());
        verify(notificationStatusService).findByOrderId(ORDER_ID);
    }

    @Test
    void getByOrderIdReturnsNotFoundWhenOrderIsNotAccessible() {
        when(request.getHeader("Authorization"))
                .thenReturn(AUTHORIZATION);
        when(orderOwnershipClient.isOrderAccessible(
                ORDER_ID, AUTHORIZATION))
                .thenReturn(false);

        ResponseEntity<List<NotificationStatusRecord>> response =
                notificationStatusController.getByOrderId(
                        ORDER_ID, request);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verifyNoInteractions(notificationStatusService);
    }
}