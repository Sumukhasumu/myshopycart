package com.myshopycart.notificationservice.sms;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.http.HttpHeaders.ACCEPT;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static org.springframework.http.HttpMethod.POST;

class Msg91SmsProviderTest {

    private static final String AUTH_KEY =
            "test-auth-key";

    private static final String TEMPLATE_ID =
            "test-template-id";

    private RestClient.Builder restClientBuilder;

    private MockRestServiceServer mockServer;

    private Msg91SmsProvider smsProvider;

    @BeforeEach
    void setUp() {

        restClientBuilder = RestClient.builder();

        mockServer =
                MockRestServiceServer
                        .bindTo(restClientBuilder)
                        .build();

        smsProvider =
                new Msg91SmsProvider(
                        restClientBuilder,
                        new ObjectMapper(),
                        AUTH_KEY,
                        TEMPLATE_ID);
    }

    @Test
    void shouldSendSmsThroughMsg91FlowApi() {

        mockServer.expect(
                        requestTo(
                                "https://control.msg91.com/api/v5/flow"))
                .andExpect(method(POST))
                .andExpect(header(
                        "authkey",
                        AUTH_KEY))
                .andExpect(header(
                        ACCEPT,
                        APPLICATION_JSON_VALUE))
                .andExpect(jsonPath(
                        "$.template_id")
                        .value(TEMPLATE_ID))
                .andExpect(jsonPath(
                        "$.short_url")
                        .value("0"))
                .andExpect(jsonPath(
                        "$.recipients[0].mobiles")
                        .value("919449966454"))
                .andExpect(jsonPath(
                        "$.recipients[0].message")
                        .value(
                                "MYSHOPYCART: Order #101 confirmed."))
                .andRespond(
                        withSuccess(
                                """
                                {
                                  "message": "test-request-id",
                                  "type": "success"
                                }
                                """,
                                APPLICATION_JSON));

        assertDoesNotThrow(() ->
                smsProvider.send(
                        "9449966454",
                        "MYSHOPYCART: Order #101 confirmed."));

        mockServer.verify();
    }

    @Test
    void shouldAcceptMobileNumberWithCountryCode() {

        mockServer.expect(
                        requestTo(
                                "https://control.msg91.com/api/v5/flow"))
                .andExpect(jsonPath(
                        "$.recipients[0].mobiles")
                        .value("919449966454"))
                .andRespond(
                        withSuccess(
                                """
                                {
                                  "message": "test-request-id",
                                  "type": "success"
                                }
                                """,
                                APPLICATION_JSON));

        assertDoesNotThrow(() ->
                smsProvider.send(
                        "+919449966454",
                        "Test SMS"));

        mockServer.verify();
    }

    @Test
    void shouldRejectBlankRecipientMobile() {

        assertThrows(
                IllegalArgumentException.class,
                () -> smsProvider.send(
                        "",
                        "Test SMS"));
    }

    @Test
    void shouldRejectNullRecipientMobile() {

        assertThrows(
                IllegalArgumentException.class,
                () -> smsProvider.send(
                        null,
                        "Test SMS"));
    }

    @Test
    void shouldRejectInvalidIndianMobileNumber() {

        assertThrows(
                IllegalArgumentException.class,
                () -> smsProvider.send(
                        "1234567890",
                        "Test SMS"));
    }

    @Test
    void shouldRejectBlankMessage() {

        assertThrows(
                IllegalArgumentException.class,
                () -> smsProvider.send(
                        "9449966454",
                        ""));
    }

    @Test
    void shouldRejectNullMessage() {

        assertThrows(
                IllegalArgumentException.class,
                () -> smsProvider.send(
                        "9449966454",
                        null));
    }

    @Test
    void shouldRejectMissingAuthKey() {

        Msg91SmsProvider provider =
                new Msg91SmsProvider(
                        restClientBuilder,
                        new ObjectMapper(),
                        "",
                        TEMPLATE_ID);

        assertThrows(
                IllegalStateException.class,
                () -> provider.send(
                        "9449966454",
                        "Test SMS"));
    }

    @Test
    void shouldRejectMissingTemplateId() {

        Msg91SmsProvider provider =
                new Msg91SmsProvider(
                        restClientBuilder,
                        new ObjectMapper(),
                        AUTH_KEY,
                        "");

        assertThrows(
                IllegalStateException.class,
                () -> provider.send(
                        "9449966454",
                        "Test SMS"));
    }

    @Test
    void shouldRejectMsg91ErrorResponse() {

        mockServer.expect(
                        requestTo(
                                "https://control.msg91.com/api/v5/flow"))
                .andRespond(
                        withSuccess(
                                """
                                {
                                  "message": "flow id missing",
                                  "type": "error"
                                }
                                """,
                                APPLICATION_JSON));

        assertThrows(
                IllegalStateException.class,
                () -> smsProvider.send(
                        "9449966454",
                        "Test SMS"));

        mockServer.verify();
    }
}