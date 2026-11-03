package com.myshopycart.notificationservice.sms;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class Msg91SmsProvider implements SmsProvider {

    private static final String MSG91_FLOW_API_URL =
            "https://control.msg91.com/api/v5/flow";

    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    private final String authKey;
    private final String templateId;

    public Msg91SmsProvider(
            RestClient.Builder restClientBuilder,
            ObjectMapper objectMapper,
            @Value("${myshopycart.msg91.auth-key:}") String authKey,
            @Value("${myshopycart.msg91.template-id:}") String templateId) {

        this.restClient = restClientBuilder.build();
        this.objectMapper = objectMapper;
        this.authKey = authKey;
        this.templateId = templateId;
    }

    @Override
    public void send(
            String recipientMobile,
            String message) {

        validateRecipientMobile(recipientMobile);
        validateMessage(message);
        validateConfiguration();

        String mobile =
                normalizeIndianMobileNumber(recipientMobile);

        Map<String, Object> recipient = Map.of(
                "mobiles", mobile,
                "message", message
        );

        Map<String, Object> requestBody = Map.of(
                "template_id", templateId,
                "short_url", "0",
                "recipients", List.of(recipient)
        );

        log.info(
                "Sending SMS through MSG91: recipient={}, templateId={}",
                maskMobile(mobile),
                templateId);

        String response;

        try {

            response = restClient.post()
                    .uri(MSG91_FLOW_API_URL)
                    .header("authkey", authKey)
                    .header(
                            HttpHeaders.ACCEPT,
                            MediaType.APPLICATION_JSON_VALUE)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(requestBody)
                    .retrieve()
                    .body(String.class);

        } catch (RuntimeException e) {

            throw new IllegalStateException(
                    "MSG91 SMS request failed",
                    e);
        }

        validateMsg91Response(response);

        log.info(
                "MSG91 SMS request accepted: recipient={}, templateId={}",
                maskMobile(mobile),
                templateId);
    }

    private void validateConfiguration() {

        if (authKey == null || authKey.isBlank()) {

            throw new IllegalStateException(
                    "MSG91 auth key is not configured. " +
                    "Set MYSHOPYCART_MSG91_AUTH_KEY.");
        }

        if (templateId == null || templateId.isBlank()) {

            throw new IllegalStateException(
                    "MSG91 template ID is not configured. " +
                    "Set MYSHOPYCART_TEMPLATE_ID.");
        }
    }

    private String normalizeIndianMobileNumber(
            String recipientMobile) {

        String mobile =
                recipientMobile.trim()
                        .replaceAll("[\\s-]", "");

        if (mobile.startsWith("+91")) {

            mobile = mobile.substring(1);

        } else if (mobile.startsWith("91")
                && mobile.length() == 12) {

            // Already in international format.

        } else if (mobile.matches("\\d{10}")) {

            mobile = "91" + mobile;

        } else {

            throw new IllegalArgumentException(
                    "Recipient mobile number must be a valid " +
                    "10-digit Indian mobile number or use " +
                    "international format +91XXXXXXXXXX");
        }

        if (!mobile.matches("91[6-9]\\d{9}")) {

            throw new IllegalArgumentException(
                    "Recipient mobile number must be a valid " +
                    "Indian mobile number");
        }

        return mobile;
    }

    private void validateMsg91Response(
            String response) {

        if (response == null || response.isBlank()) {

            throw new IllegalStateException(
                    "MSG91 returned an empty response");
        }

        try {

            JsonNode jsonResponse =
                    objectMapper.readTree(response);

            JsonNode typeNode =
                    jsonResponse.get("type");

            if (typeNode != null
                    && "success".equalsIgnoreCase(
                    typeNode.asText())) {

                return;
            }

            JsonNode messageNode =
                    jsonResponse.get("message");

            String errorMessage =
                    messageNode != null
                            ? messageNode.asText()
                            : response;

            throw new IllegalStateException(
                    "MSG91 rejected SMS request: "
                            + errorMessage);

        } catch (IllegalStateException e) {

            throw e;

        } catch (Exception e) {

            throw new IllegalStateException(
                    "Unable to parse MSG91 response",
                    e);
        }
    }

    private void validateRecipientMobile(
            String recipientMobile) {

        if (recipientMobile == null
                || recipientMobile.isBlank()) {

            throw new IllegalArgumentException(
                    "Recipient mobile number must not be blank");
        }
    }

    private void validateMessage(
            String message) {

        if (message == null
                || message.isBlank()) {

            throw new IllegalArgumentException(
                    "SMS message must not be blank");
        }
    }

    private String maskMobile(
            String mobile) {

        if (mobile == null || mobile.length() < 4) {
            return "****";
        }

        return "******" +
                mobile.substring(mobile.length() - 4);
    }
}