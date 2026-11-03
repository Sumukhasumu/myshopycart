package com.myshopycart.notificationservice.notification.status;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class NotificationStatusValidationTest {

    @Mock
    private NotificationStatusRepository notificationStatusRepository;

    @Test
    void shouldRejectBlankMessageId() {

        NotificationStatusService service =
                new NotificationStatusService(
                        notificationStatusRepository);

        assertThrows(
                IllegalArgumentException.class,
                () -> service.findByMessageId("   "));

        verifyNoInteractions(notificationStatusRepository);
    }

    @Test
    void shouldRejectNullMessageId() {

        NotificationStatusService service =
                new NotificationStatusService(
                        notificationStatusRepository);

        assertThrows(
                IllegalArgumentException.class,
                () -> service.findByMessageId(null));

        verifyNoInteractions(notificationStatusRepository);
    }

    @Test
    void shouldRejectInvalidOrderId() {

        NotificationStatusService service =
                new NotificationStatusService(
                        notificationStatusRepository);

        assertThrows(
                IllegalArgumentException.class,
                () -> service.findByOrderId(0L));

        verifyNoInteractions(notificationStatusRepository);
    }

    @Test
    void shouldRejectNullOrderId() {

        NotificationStatusService service =
                new NotificationStatusService(
                        notificationStatusRepository);

        assertThrows(
                IllegalArgumentException.class,
                () -> service.findByOrderId(null));

        verifyNoInteractions(notificationStatusRepository);
    }
}