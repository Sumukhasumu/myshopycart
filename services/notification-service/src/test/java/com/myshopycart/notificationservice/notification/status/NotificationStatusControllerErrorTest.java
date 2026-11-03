package com.myshopycart.notificationservice.notification.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Bean;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(NotificationStatusController.class)
@Import(NotificationStatusControllerErrorTest.TestSecurityConfig.class)
class NotificationStatusControllerErrorTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private NotificationStatusService notificationStatusService;

    @MockBean
    private OrderOwnershipClient orderOwnershipClient;

    @Test
    void shouldReturnNotFoundWhenMessageStatusDoesNotExist()
            throws Exception {

        when(notificationStatusService.findByMessageId("missing-message"))
                .thenThrow(new IllegalArgumentException(
                        "Notification status not found for message: missing-message"));

        mockMvc.perform(
                get("/api/v1/notifications/status/missing-message"))
                .andExpect(status().isNotFound());

        verify(notificationStatusService).findByMessageId("missing-message");
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