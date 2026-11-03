package com.myshopycart.orderservice.service;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.myshopycart.orderservice.entity.Invoice;
import com.myshopycart.orderservice.entity.InvoiceAddress;
import com.myshopycart.orderservice.entity.InvoiceItem;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
public class InvoicePdfGenerator {

    public byte[] generate(Invoice invoice) {

        if (invoice == null) {
            throw new IllegalArgumentException(
                    "Invoice must not be null");
        }

        if (invoice.getInvoiceNumber() == null ||
                invoice.getInvoiceNumber().isBlank()) {
            throw new IllegalArgumentException(
                    "Invoice number must not be blank");
        }

        ByteArrayOutputStream outputStream =
                new ByteArrayOutputStream();

        try {
            Document document = new Document();

            PdfWriter.getInstance(
                    document,
                    outputStream);

            document.open();

            addHeader(document, invoice);
            addCustomerSection(document, invoice);
            addAddressSection(document, invoice);
            addItemsTable(document, invoice);
            addTotalsSection(document, invoice);
            addFooter(document);

            document.close();

            return outputStream.toByteArray();

        } catch (DocumentException exception) {

            throw new IllegalStateException(
                    "Failed to generate invoice PDF",
                    exception);
        }
    }

    private void addHeader(
            Document document,
            Invoice invoice) throws DocumentException {

        Font titleFont =
                FontFactory.getFont(
                        FontFactory.HELVETICA_BOLD,
                        20);

        Font normalFont =
                FontFactory.getFont(
                        FontFactory.HELVETICA,
                        10);

        Paragraph title =
                new Paragraph(
                        "MYSHOPYCART",
                        titleFont);

        title.setAlignment(
                Element.ALIGN_CENTER);

        document.add(title);

        Paragraph invoiceTitle =
                new Paragraph(
                        "TAX INVOICE",
                        FontFactory.getFont(
                                FontFactory.HELVETICA_BOLD,
                                14));

        invoiceTitle.setAlignment(
                Element.ALIGN_CENTER);

        document.add(invoiceTitle);

        document.add(
                new Paragraph(
                        "Invoice Number: "
                                + invoice.getInvoiceNumber(),
                        normalFont));

        document.add(
                new Paragraph(
                        "Issue Date: "
                                + String.valueOf(
                                        invoice.getIssueDate()),
                        normalFont));

        document.add(
                new Paragraph(" "));
    }

    private void addCustomerSection(
            Document document,
            Invoice invoice) throws DocumentException {

        Font headingFont =
                FontFactory.getFont(
                        FontFactory.HELVETICA_BOLD,
                        11);

        Font normalFont =
                FontFactory.getFont(
                        FontFactory.HELVETICA,
                        10);

        document.add(
                new Paragraph(
                        "Customer Details",
                        headingFont));

        document.add(
                new Paragraph(
                        "Name: "
                                + safe(
                                        invoice.getCustomerName()),
                        normalFont));

        document.add(
                new Paragraph(
                        "Email: "
                                + safe(
                                        invoice.getCustomerEmail()),
                        normalFont));

        if (hasText(invoice.getCustomerPhone())) {
            document.add(
                    new Paragraph(
                            "Phone: "
                                    + invoice.getCustomerPhone(),
                            normalFont));
        }

        if (hasText(invoice.getCustomerGstin())) {
            document.add(
                    new Paragraph(
                            "GSTIN: "
                                    + invoice.getCustomerGstin(),
                            normalFont));
        }

        document.add(
                new Paragraph(" "));
    }

    private void addAddressSection(
            Document document,
            Invoice invoice) throws DocumentException {

        Font headingFont =
                FontFactory.getFont(
                        FontFactory.HELVETICA_BOLD,
                        11);

        PdfPTable addressTable =
                new PdfPTable(2);

        addressTable.setWidthPercentage(100);

        PdfPCell billingCell =
                createAddressCell(
                        "Billing Address",
                        invoice.getBillingAddress(),
                        headingFont);

        PdfPCell shippingCell =
                createAddressCell(
                        "Shipping Address",
                        invoice.getShippingAddress(),
                        headingFont);

        addressTable.addCell(billingCell);
        addressTable.addCell(shippingCell);

        document.add(addressTable);

        document.add(
                new Paragraph(" "));
    }

    private PdfPCell createAddressCell(
            String title,
            InvoiceAddress address,
            Font headingFont) {

        PdfPCell cell =
                new PdfPCell();

        cell.setBorder(Rectangle.BOX);
        cell.setPadding(8);

        Paragraph paragraph =
                new Paragraph(
                        title,
                        headingFont);

        cell.addElement(paragraph);

        if (address == null) {
            cell.addElement(
                    new Paragraph("Not available"));

            return cell;
        }

        cell.addElement(
                new Paragraph(
                        safe(address.getAddress())));

        cell.addElement(
                new Paragraph(
                        safe(address.getCity())
                                + ", "
                                + safe(address.getState())));

        cell.addElement(
                new Paragraph(
                        safe(address.getPostalCode())));

        cell.addElement(
                new Paragraph(
                        safe(address.getCountry())));

        return cell;
    }

    private void addItemsTable(
            Document document,
            Invoice invoice) throws DocumentException {

        Font headingFont =
                FontFactory.getFont(
                        FontFactory.HELVETICA_BOLD,
                        9);

        Font normalFont =
                FontFactory.getFont(
                        FontFactory.HELVETICA,
                        9);

        PdfPTable table =
                new PdfPTable(5);

        table.setWidthPercentage(100);

        table.setWidths(
                new float[]{
                        3.5f,
                        1.2f,
                        1.5f,
                        1.5f,
                        1.5f
                });

        addHeaderCell(
                table,
                "Product",
                headingFont);

        addHeaderCell(
                table,
                "Qty",
                headingFont);

        addHeaderCell(
                table,
                "Unit Price",
                headingFont);

        addHeaderCell(
                table,
                "Line Total",
                headingFont);

        addHeaderCell(
                table,
                "Product ID",
                headingFont);

        List<InvoiceItem> items =
                invoice.getItems();

        if (items != null) {

            for (InvoiceItem item : items) {

                table.addCell(
                        new Phrase(
                                safe(
                                        item.getProductName()),
                                normalFont));

                table.addCell(
                        new Phrase(
                                String.valueOf(
                                        item.getQuantity()),
                                normalFont));

                table.addCell(
                        new Phrase(
                                formatMoney(
                                        item.getUnitPrice()),
                                normalFont));

                table.addCell(
                        new Phrase(
                                formatMoney(
                                        item.getLineTotal()),
                                normalFont));

                table.addCell(
                        new Phrase(
                                safe(
                                        item.getProductId()),
                                normalFont));
            }
        }

        document.add(table);

        document.add(
                new Paragraph(" "));
    }

    private void addHeaderCell(
            PdfPTable table,
            String text,
            Font font) {

        PdfPCell cell =
                new PdfPCell(
                        new Phrase(
                                text,
                                font));

        cell.setHorizontalAlignment(
                Element.ALIGN_CENTER);

        cell.setPadding(5);

        table.addCell(cell);
    }

    private void addTotalsSection(
            Document document,
            Invoice invoice) throws DocumentException {

        PdfPTable totalsTable =
                new PdfPTable(2);

        totalsTable.setWidthPercentage(40);

        totalsTable.setHorizontalAlignment(
                Element.ALIGN_RIGHT);

        addTotalRow(
                totalsTable,
                "Subtotal",
                formatMoney(
                        invoice.getSubtotal()));

        addTotalRow(
                totalsTable,
                "Tax",
                formatMoney(
                        invoice.getTaxAmount()));

        addTotalRow(
                totalsTable,
                "Total",
                formatMoney(
                        invoice.getTotalAmount()));

        document.add(totalsTable);

        document.add(
                new Paragraph(" "));

        document.add(
                new Paragraph(
                        "Payment Method: "
                                + safe(
                                        invoice.getPaymentMethod())));
    }

    private void addTotalRow(
            PdfPTable table,
            String label,
            String value) {

        table.addCell(
                new Phrase(label));

        PdfPCell valueCell =
                new PdfPCell(
                        new Phrase(value));

        valueCell.setHorizontalAlignment(
                Element.ALIGN_RIGHT);

        table.addCell(valueCell);
    }

    private void addFooter(
            Document document) throws DocumentException {

        Paragraph footer =
                new Paragraph(
                        "Thank you for shopping with MYSHOPYCART.");

        footer.setAlignment(
                Element.ALIGN_CENTER);

        document.add(footer);
    }

    private String formatMoney(
            BigDecimal value) {

        if (value == null) {
            return "0.00";
        }

        return value
                .setScale(
                        2,
                        RoundingMode.HALF_UP)
                .toPlainString();
    }

    private String safe(
            String value) {

        return value == null
                ? ""
                : value;
    }

    private boolean hasText(
            String value) {

        return value != null &&
                !value.isBlank();
    }
}