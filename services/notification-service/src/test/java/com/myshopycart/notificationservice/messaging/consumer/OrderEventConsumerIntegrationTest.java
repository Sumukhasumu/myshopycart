package com.myshopycart.notificationservice.messaging.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.myshopycart.notificationservice.messaging.event.MyShopyCartEvent;
import com.myshopycart.notificationservice.messaging.idempotency.ProcessedMessage;
import com.myshopycart.notificationservice.messaging.idempotency.ProcessedMessageRepository;
import com.myshopycart.notificationservice.messaging.message.MyShopyCartMessage;
import com.myshopycart.notificationservice.messaging.producer.NotificationTaskProducer;
import com.myshopycart.notificationservice.notification.OrderNotificationPayload;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.TestPropertySource;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

@SpringBootTest
@EmbeddedKafka(
        partitions = 1,
        topics = OrderEventConsumer.ORDER_EVENTS_TOPIC,
        bootstrapServersProperty = "spring.kafka.bootstrap-servers"
)
@TestPropertySource(properties = {
        "myshopycart.kafka.notification-group-id=myshopycart-notification-integration-test",
        "spring.kafka.consumer.auto-offset-reset=earliest",
        "spring.kafka.producer.key-serializer=org.apache.kafka.common.serialization.StringSerializer",
        "spring.kafka.producer.value-serializer=org.springframework.kafka.support.serializer.JsonSerializer"
})
class OrderEventConsumerIntegrationTest {

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Autowired
    private ProcessedMessageRepository processedMessageRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private NotificationTaskProducer notificationTaskProducer;

    @Test
    void shouldPreserveInvoicePdfBytesAcrossKafka()
            throws Exception {

        String eventId =
                "kafka-integration-" + UUID.randomUUID();

        String aggregateId =
                "integration-order-" + UUID.randomUUID();

        /*
         * Binary data representing the PDF generated
         * by InvoicePdfGenerator.
         *
         * Values above 127 are intentionally included
         * to verify that binary data is preserved correctly.
         */
        byte[] originalInvoicePdf = new byte[] {
                '%', 'P', 'D', 'F', '-',
                '1', '.', '7', '\n',
                0x25, (byte) 0xE2, 0x28, (byte) 0xA1,
                0x00, 0x01, 0x7F,
                0x10, 0x20, 0x30, 0x40,
                'T', 'E', 'S', 'T',
                '\n'
        };

        OrderNotificationPayload payload =
                new OrderNotificationPayload(
                        1001L,
                        25L,
                        "Integration Test Customer",
                        "integration@example.com",
                        "9876543210",
                        new BigDecimal("2000.00"),
                        "INV-INTEGRATION-000001",
                        originalInvoicePdf
                );

        MyShopyCartEvent<OrderNotificationPayload> event =
                new MyShopyCartEvent<>(
                        eventId,
                        "ORDER_CONFIRMED",
                        1,
                        Instant.now(),
                        "order-service-test",
                        "ORDER",
                        aggregateId,
                        UUID.randomUUID().toString(),
                        null,
                        payload
                );

        ProducerRecord<String, Object> record =
                new ProducerRecord<>(
                        OrderEventConsumer.ORDER_EVENTS_TOPIC,
                        aggregateId,
                        event
                );

        kafkaTemplate.send(record).get();

        /*
         * Wait until OrderEventConsumer receives the
         * Kafka message and publishes the Rabbit task.
         */
        ArgumentCaptor<MyShopyCartMessage<?>>
                messageCaptor =
                ArgumentCaptor.forClass(
                        MyShopyCartMessage.class);

        verify(
                notificationTaskProducer,
                timeout(15000)
        ).publish(messageCaptor.capture());

        MyShopyCartMessage<?> notificationMessage =
                messageCaptor.getValue();

        assertNotNull(
                notificationMessage,
                "Notification message must not be null"
        );

        assertEquals(
                "SEND_ORDER_CONFIRMATION",
                notificationMessage.messageType()
        );

        assertEquals(
                eventId,
                notificationMessage.causationId()
        );

        /*
         * Convert the deserialized payload into the exact
         * notification payload type used by the application.
         */
        OrderNotificationPayload receivedPayload =
                objectMapper.convertValue(
                        notificationMessage.payload(),
                        OrderNotificationPayload.class
                );

        assertNotNull(
                receivedPayload,
                "Deserialized notification payload must not be null"
        );

        assertEquals(
                1001L,
                receivedPayload.orderId()
        );

        assertEquals(
                "INV-INTEGRATION-000001",
                receivedPayload.invoiceNumber()
        );

        assertNotNull(
                receivedPayload.invoicePdf(),
                "Deserialized invoice PDF must not be null"
        );

        /*
         * THE IMPORTANT ASSERTION
         *
         * This proves whether the original PDF bytes survived
         * Kafka JSON serialization and deserialization.
         */
        assertArrayEquals(
                originalInvoicePdf,
                receivedPayload.invoicePdf(),
                "Invoice PDF bytes changed while passing through Kafka"
        );

        /*
         * Verify the Kafka event was also recorded as processed.
         */
        long deadline =
                System.currentTimeMillis()
                        + 15000;

        ProcessedMessage processedEvent = null;

        while (System.currentTimeMillis() < deadline) {

            processedEvent =
                    processedMessageRepository
                            .findById(eventId)
                            .orElse(null);

            if (processedEvent != null) {
                break;
            }

            Thread.sleep(250);
        }

        assertNotNull(
                processedEvent,
                "Kafka ORDER_CONFIRMED event was not processed within 15 seconds"
        );

        assertEquals(
                "ORDER_CONFIRMED",
                processedEvent.getMessageType()
        );

        assertEquals(
                "order-service-test",
                processedEvent.getSource()
        );

        /*
         * Cleanup the idempotency record created by this test.
         */
        processedMessageRepository.deleteById(eventId);
    }
}