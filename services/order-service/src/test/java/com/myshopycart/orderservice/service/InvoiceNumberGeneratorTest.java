package com.myshopycart.orderservice.service;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@Import(InvoiceNumberGenerator.class)
@ActiveProfiles("test")
@Testcontainers
@AutoConfigureTestDatabase(
        replace = AutoConfigureTestDatabase.Replace.NONE
)
class InvoiceNumberGeneratorTest {

    @Container
    static PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:15-alpine")
                    .withDatabaseName("myshopycart_order_test")
                    .withUsername("test")
                    .withPassword("test");

    @DynamicPropertySource
    static void configureDatasource(
            DynamicPropertyRegistry registry) {

        registry.add(
                "spring.datasource.url",
                postgres::getJdbcUrl
        );

        registry.add(
                "spring.datasource.username",
                postgres::getUsername
        );

        registry.add(
                "spring.datasource.password",
                postgres::getPassword
        );

        registry.add(
                "spring.datasource.driver-class-name",
                postgres::getDriverClassName
        );
    }

    @BeforeAll
    static void createInvoiceNumberSequence()
            throws Exception {

        try (Connection connection =
                     DriverManager.getConnection(
                             postgres.getJdbcUrl(),
                             postgres.getUsername(),
                             postgres.getPassword());
             Statement statement =
                     connection.createStatement()) {

            statement.execute(
                    "CREATE SEQUENCE IF NOT EXISTS " +
                    "invoice_number_seq " +
                    "START WITH 1 INCREMENT BY 1"
            );
        }
    }

    @Autowired
    private InvoiceNumberGenerator invoiceNumberGenerator;

    @Test
    void shouldGenerateInvoiceNumberWithExpectedFormat() {

        String invoiceNumber =
                invoiceNumberGenerator.generate();

        assertTrue(
                invoiceNumber.matches(
                        "INV-\\d{4}-\\d{6}"
                )
        );
    }

    @Test
    void shouldGenerateUniqueSequentialInvoiceNumbers() {

        String firstInvoiceNumber =
                invoiceNumberGenerator.generate();

        String secondInvoiceNumber =
                invoiceNumberGenerator.generate();

        assertTrue(
                firstInvoiceNumber.matches(
                        "INV-\\d{4}-\\d{6}"
                )
        );

        assertTrue(
                secondInvoiceNumber.matches(
                        "INV-\\d{4}-\\d{6}"
                )
        );

        assertNotEquals(
                firstInvoiceNumber,
                secondInvoiceNumber
        );

        int firstSequence =
                extractSequence(firstInvoiceNumber);

        int secondSequence =
                extractSequence(secondInvoiceNumber);

        assertEquals(
                firstSequence + 1,
                secondSequence
        );
    }

    private int extractSequence(
            String invoiceNumber) {

        return Integer.parseInt(
                invoiceNumber.substring(
                        invoiceNumber.length() - 6
                )
        );
    }
}