package com.myshopycart.notificationservice.email;

import jakarta.mail.BodyPart;
import jakarta.mail.Multipart;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.Test;
import org.springframework.mail.javamail.JavaMailSender;

import java.io.InputStream;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class EmailServiceAttachmentTest {

    @Test
    void shouldPreserveInvoicePdfBytesInEmailAttachment()
            throws Exception {

        JavaMailSender mailSender =
                mock(JavaMailSender.class);

        Session session =
                Session.getInstance(
                        new Properties());

        MimeMessage mimeMessage =
                new MimeMessage(session);

        when(mailSender.createMimeMessage())
                .thenReturn(mimeMessage);

        EmailService emailService =
                new EmailService(
                        mailSender,
                        "sumu0588s@gmail.com");

        byte[] originalInvoicePdf =
                new byte[] {
                        '%', 'P', 'D', 'F', '-',
                        '1', '.', '7', '\n',
                        0x25, (byte) 0xE2,
                        0x28, (byte) 0xA1,
                        0x00, 0x01, 0x7F,
                        0x10, 0x20, 0x30, 0x40,
                        'M', 'Y', 'S', 'H',
                        'O', 'P', 'Y', 'C', 'A', 'R', 'T',
                        '\n'
                };

        emailService.sendOrderConfirmationEmail(
                "customer@example.com",
                "Test Customer",
                2001L,
                new java.math.BigDecimal("2600.00"),
                "INV-TEST-000001",
                originalInvoicePdf
        );

        Object content =
                mimeMessage.getContent();

        assertNotNull(
                content,
                "Email MIME content must not be null");

        assertTrue(
                content instanceof Multipart,
                "Email must contain multipart content");

        Multipart multipart =
                (Multipart) content;

        BodyPart attachmentPart = null;

        for (int i = 0;
             i < multipart.getCount();
             i++) {

            BodyPart bodyPart =
                    multipart.getBodyPart(i);

            String fileName =
                    bodyPart.getFileName();

            if (fileName != null
                    && fileName.endsWith(".pdf")) {

                attachmentPart = bodyPart;
                break;
            }
        }

        assertNotNull(
                attachmentPart,
                "PDF attachment must be present");

        assertEquals(
                "INV-TEST-000001.pdf",
                attachmentPart.getFileName());

        System.out.println(
                "========== EMAIL ATTACHMENT DIAGNOSTIC ==========");

        System.out.println(
                "Attachment filename: "
                        + attachmentPart.getFileName());

        System.out.println(
                "Attachment Content-Type: "
                        + attachmentPart.getContentType());

        System.out.println(
                "Attachment Content-Disposition: "
                        + attachmentPart.getHeader(
                                "Content-Disposition"));

        System.out.println(
                "Attachment Content-Transfer-Encoding: "
                        + attachmentPart.getHeader(
                                "Content-Transfer-Encoding"));

        System.out.println(
                "==================================================");

        assertNotNull(
                attachmentPart.getContentType(),
                "Attachment Content-Type must not be null");

        byte[] attachmentBytes;

        try (InputStream inputStream =
                     attachmentPart.getInputStream()) {

            attachmentBytes =
                    inputStream.readAllBytes();
        }

        assertArrayEquals(
                originalInvoicePdf,
                attachmentBytes,
                "Invoice PDF bytes changed while creating email MIME attachment"
        );
    }
}
