package com.myshopycart.notificationservice.email;

import jakarta.mail.internet.MimeMessage;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@Disabled("Manual real Gmail SMTP verification test")
@SpringBootTest
@ActiveProfiles("test")
class GmailSmtpTest {

    @Autowired
    private JavaMailSender mailSender;

    @Test
    void shouldConnectToGmailSmtp() throws Exception {

        MimeMessage message =
                mailSender.createMimeMessage();

        message.setFrom("sumu0588s@gmail.com");
        message.setRecipients(
                MimeMessage.RecipientType.TO,
                "sumu0588s@gmail.com");
        message.setSubject(
                "MYSHOPYCART Gmail SMTP Test");
        message.setText(
                """
                Hello,

                This is a real SMTP connectivity test
                from the MYSHOPYCART notification-service.

                If you received this email, Gmail SMTP
                authentication is working successfully.

                MYSHOPYCART Team
                """);

        assertNotNull(message);

        mailSender.send(message);
    }
}