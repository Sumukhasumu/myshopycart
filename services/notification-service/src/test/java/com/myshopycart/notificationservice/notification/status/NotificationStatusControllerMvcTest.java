package com.myshopycart.notificationservice.notification.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(NotificationStatusController.class)
@Import(NotificationStatusControllerMvcTest.TestSecurityConfig.class)
class NotificationStatusControllerMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private NotificationStatusService notificationStatusService;

    @MockBean
    private OrderOwnershipClient orderOwnershipClient;

    @Test
    void shouldGetStatusByMessageId() throws Exception {

        NotificationStatusRecord record =
                createRecord(
                        "message-001",
                        101L,
                        NotificationChannel.EMAIL,
                        NotificationStatus.SENT,
                        "customer@example.com");

        when(notificationStatusService.findByMessageId("message-001"))
                .thenReturn(record);

        when(orderOwnershipClient.isOrderAccessible(101L, null))
                .thenReturn(true);

        mockMvc.perform(
                get("/api/v1/notifications/status/message-001")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.messageId").value("message-001"))
                .andExpect(jsonPath("$.orderId").value(101))
                .andExpect(jsonPath("$.channel").value("EMAIL"))
                .andExpect(jsonPath("$.status").value("SENT"))
                .andExpect(jsonPath("$.recipient")
                        .value("customer@example.com"));

        verify(orderOwnershipClient)
                .isOrderAccessible(101L, null);
    }

    @Test
    void shouldGetStatusByOrderId() throws Exception {

        NotificationStatusRecord emailRecord =
                createRecord(
                        "message-001-EMAIL",
                        101L,
                        NotificationChannel.EMAIL,
                        NotificationStatus.SENT,
                        "customer@example.com");

        NotificationStatusRecord smsRecord =
                createRecord(
                        "message-001-SMS",
                        101L,
                        NotificationChannel.SMS,
                        NotificationStatus.FAILED,
                        "1234567890");

        when(orderOwnershipClient.isOrderAccessible(101L, null))
                .thenReturn(true);

        when(notificationStatusService.findByOrderId(101L))
                .thenReturn(List.of(emailRecord, smsRecord));

        mockMvc.perform(
                get("/api/v1/notifications/status/order/101")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].messageId")
                        .value("message-001-EMAIL"))
                .andExpect(jsonPath("$[0].channel").value("EMAIL"))
                .andExpect(jsonPath("$[0].status").value("SENT"))
                .andExpect(jsonPath("$[1].messageId")
                        .value("message-001-SMS"))
                .andExpect(jsonPath("$[1].channel").value("SMS"))
                .andExpect(jsonPath("$[1].status").value("FAILED"));

        verify(orderOwnershipClient)
                .isOrderAccessible(101L, null);
    }

    private NotificationStatusRecord createRecord(
            String messageId,
            Long orderId,
            NotificationChannel channel,
            NotificationStatus status,
            String recipient) {

        Instant now = Instant.now();

        NotificationStatusRecord record =
                new NotificationStatusRecord();

        record.setMessageId(messageId);
        record.setOrderId(orderId);
        record.setChannel(channel);
        record.setStatus(status);
        record.setRecipient(recipient);
        record.setCreatedAt(now);
        record.setUpdatedAt(now);

        return record;
    }

    @TestConfiguration
    static class TestSecurityConfig {

        @Bean
        SecurityFilterChain testSecurityFilterChain(HttpSecurity http)
                throws Exception {

            http.csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());

            return http.build();
        }
    }
}