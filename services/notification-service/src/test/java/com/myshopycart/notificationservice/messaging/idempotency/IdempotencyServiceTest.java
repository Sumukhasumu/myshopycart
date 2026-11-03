package com.myshopycart.notificationservice.messaging.idempotency;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IdempotencyServiceTest {

    @Mock
    private ProcessedMessageRepository processedMessageRepository;

    @InjectMocks
    private IdempotencyService idempotencyService;

    @Test
    void shouldReturnFalseWhenMessageHasNotBeenProcessed() {

        String messageId = "message-001";

        when(processedMessageRepository.existsByMessageId(messageId))
                .thenReturn(false);

        boolean result =
                idempotencyService.alreadyProcessed(messageId);

        assertFalse(result);

        verify(processedMessageRepository)
                .existsByMessageId(messageId);
    }

    @Test
    void shouldReturnTrueWhenMessageHasAlreadyBeenProcessed() {

        String messageId = "message-001";

        when(processedMessageRepository.existsByMessageId(messageId))
                .thenReturn(true);

        boolean result =
                idempotencyService.alreadyProcessed(messageId);

        assertTrue(result);

        verify(processedMessageRepository)
                .existsByMessageId(messageId);
    }

    @Test
    void shouldSaveMessageWhenMarkedAsProcessed() {

        String messageId = "message-001";
        String messageType = "SEND_ORDER_CONFIRMATION";
        String source = "notification-service";

        idempotencyService.markProcessed(
                messageId,
                messageType,
                source
        );

        verify(processedMessageRepository)
                .save(any(ProcessedMessage.class));
    }
}