package com.myshopycart.notificationservice.messaging.idempotency;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class IdempotencyServiceIntegrationTest {

    private static final String TEST_MESSAGE_ID =
            "integration-test-" + System.currentTimeMillis();

    @Autowired
    private IdempotencyService idempotencyService;

    @Autowired
    private ProcessedMessageRepository processedMessageRepository;

    @Test
    void shouldPersistAndDetectProcessedMessage() {

        assertFalse(
                idempotencyService.alreadyProcessed(TEST_MESSAGE_ID)
        );

        idempotencyService.markProcessed(
                TEST_MESSAGE_ID,
                "INTEGRATION_TEST",
                "notification-service-test"
        );

        assertTrue(
                idempotencyService.alreadyProcessed(TEST_MESSAGE_ID)
        );

        processedMessageRepository.deleteById(TEST_MESSAGE_ID);
    }
}