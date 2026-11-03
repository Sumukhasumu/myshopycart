package com.myshopycart.orderservice.service;

import com.myshopycart.orderservice.entity.Invoice;
import com.myshopycart.orderservice.entity.InvoiceAddress;
import com.myshopycart.orderservice.entity.InvoiceItem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import com.lowagie.text.pdf.PdfReader;
import java.io.IOException;

class InvoicePdfGeneratorTest {

    private InvoicePdfGenerator invoicePdfGenerator;

    private Invoice invoice;

    @BeforeEach
    void setUp() {

        invoicePdfGenerator =
                new InvoicePdfGenerator();

        InvoiceAddress billingAddress =
                InvoiceAddress.builder()
                        .address("Main Road")
                        .city("Kundapura")
                        .state("Karnataka")
                        .postalCode("576201")
                        .country("India")
                        .build();

        InvoiceAddress shippingAddress =
                InvoiceAddress.builder()
                        .address("Main Road")
                        .city("Kundapura")
                        .state("Karnataka")
                        .postalCode("576201")
                        .country("India")
                        .build();

        invoice =
                Invoice.builder()
                        .id(1L)
                        .invoiceNumber("INV-2026-000001")
                        .orderId(100L)
                        .issueDate(LocalDate.of(
                                2026,
                                9,
                                29))
                        .customerName("Test Customer")
                        .customerEmail(
                                "customer@example.com")
                        .customerPhone("9876543210")
                        .customerGstin("GSTIN123")
                        .billingAddress(billingAddress)
                        .shippingAddress(shippingAddress)
                        .paymentMethod("CARD")
                        .subtotal(
                                new BigDecimal("1100.00"))
                        .taxAmount(BigDecimal.ZERO)
                        .totalAmount(
                                new BigDecimal("1100.00"))
                        .createdAt(Instant.now())
                        .updatedAt(Instant.now())
                        .items(new ArrayList<>())
                        .build();

        InvoiceItem firstItem =
                InvoiceItem.builder()
                        .invoice(invoice)
                        .productId("P100")
                        .productName("Laptop")
                        .quantity(2)
                        .unitPrice(
                                new BigDecimal("500.00"))
                        .lineTotal(
                                new BigDecimal("1000.00"))
                        .build();

        InvoiceItem secondItem =
                InvoiceItem.builder()
                        .invoice(invoice)
                        .productId("P200")
                        .productName("Mouse")
                        .quantity(1)
                        .unitPrice(
                                new BigDecimal("100.00"))
                        .lineTotal(
                                new BigDecimal("100.00"))
                        .build();

        invoice.getItems().add(firstItem);
        invoice.getItems().add(secondItem);
    }

    @Test
    void shouldGeneratePdfForInvoice() {

        byte[] pdf =
                invoicePdfGenerator.generate(invoice);

        assertNotNullAndNotEmpty(pdf);

        assertPdfHeader(pdf);
    }

    @Test
    void shouldGeneratePdfWithoutOptionalCustomerFields() {

        invoice.setCustomerPhone(null);
        invoice.setCustomerGstin(null);
        invoice.setPaymentMethod(null);

        byte[] pdf =
                invoicePdfGenerator.generate(invoice);

        assertNotNullAndNotEmpty(pdf);

        assertPdfHeader(pdf);
    }

    @Test
    void shouldGeneratePdfWithoutAddresses() {

        invoice.setBillingAddress(null);
        invoice.setShippingAddress(null);

        byte[] pdf =
                invoicePdfGenerator.generate(invoice);

        assertNotNullAndNotEmpty(pdf);

        assertPdfHeader(pdf);
    }

    @Test
    void shouldRejectNullInvoice() {

        assertThrows(
                IllegalArgumentException.class,
                () ->
                        invoicePdfGenerator.generate(null));
    }

    @Test
    void shouldRejectInvoiceWithoutInvoiceNumber() {

        invoice.setInvoiceNumber(null);

        assertThrows(
                IllegalArgumentException.class,
                () ->
                        invoicePdfGenerator.generate(invoice));
    }

    @Test
    void shouldGeneratePdfWithEmptyItemList() {

        invoice.setItems(new ArrayList<>());

        byte[] pdf =
                assertDoesNotThrow(
                        () ->
                                invoicePdfGenerator
                                        .generate(invoice));

        assertNotNullAndNotEmpty(pdf);

        assertPdfHeader(pdf);
    }

    private void assertNotNullAndNotEmpty(
            byte[] pdf) {

        assertTrue(
                pdf != null &&
                        pdf.length > 0);
    }

    private void assertPdfHeader(
        byte[] pdf) {

    String header =
            new String(
                    pdf,
                    0,
                    Math.min(pdf.length, 5));

    assertFalse(header.isBlank());

    assertTrue(
            header.startsWith("%PDF"));

    PdfReader reader = null;

    try {

        reader = new PdfReader(pdf);

        assertTrue(
                reader.getNumberOfPages() > 0,
                "Generated PDF must contain at least one page");

    } catch (IOException exception) {

        throw new AssertionError(
                "Generated PDF could not be parsed",
                exception);

    } finally {

        if (reader != null) {
            reader.close();
        		}
    		}
	}
}