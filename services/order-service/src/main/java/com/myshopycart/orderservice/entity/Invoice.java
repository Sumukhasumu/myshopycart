package com.myshopycart.orderservice.entity;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "invoices")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Invoice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Customer-facing invoice number.
     *
     * Example:
     * INV-2026-000001
     */
    @Column(
            name = "invoice_number",
            nullable = false,
            unique = true,
            length = 30
    )
    private String invoiceNumber;

    /**
     * Reference to the business order.
     *
     * One order has one invoice.
     */
    @Column(
            name = "order_id",
            nullable = false,
            unique = true
    )
    private Long orderId;

    @Column(
            name = "issue_date",
            nullable = false
    )
    private LocalDate issueDate;

    /*
     * Customer snapshot.
     *
     * These values are captured at invoice creation time.
     * They should not depend on future changes to the user profile.
     */
    @Column(
            name = "customer_name",
            nullable = false,
            length = 150
    )
    private String customerName;

    @Column(
            name = "customer_email",
            nullable = false,
            length = 255
    )
    private String customerEmail;

    @Column(
            name = "customer_phone",
            length = 30
    )
    private String customerPhone;

    @Column(
            name = "customer_gstin",
            length = 30
    )
    private String customerGstin;

    /*
     * Billing address snapshot.
     */
    @Embedded
    @AttributeOverrides({
            @AttributeOverride(
                    name = "address",
                    column = @Column(
                            name = "billing_address",
                            length = 500
                    )
            ),
            @AttributeOverride(
                    name = "city",
                    column = @Column(
                            name = "billing_city",
                            length = 100
                    )
            ),
            @AttributeOverride(
                    name = "state",
                    column = @Column(
                            name = "billing_state",
                            length = 100
                    )
            ),
            @AttributeOverride(
                    name = "postalCode",
                    column = @Column(
                            name = "billing_postal_code",
                            length = 20
                    )
            ),
            @AttributeOverride(
                    name = "country",
                    column = @Column(
                            name = "billing_country",
                            length = 100
                    )
            )
    })
    private InvoiceAddress billingAddress;

    /*
     * Shipping address snapshot.
     */
    @Embedded
    @AttributeOverrides({
            @AttributeOverride(
                    name = "address",
                    column = @Column(
                            name = "shipping_address",
                            length = 500
                    )
            ),
            @AttributeOverride(
                    name = "city",
                    column = @Column(
                            name = "shipping_city",
                            length = 100
                    )
            ),
            @AttributeOverride(
                    name = "state",
                    column = @Column(
                            name = "shipping_state",
                            length = 100
                    )
            ),
            @AttributeOverride(
                    name = "postalCode",
                    column = @Column(
                            name = "shipping_postal_code",
                            length = 20
                    )
            ),
            @AttributeOverride(
                    name = "country",
                    column = @Column(
                            name = "shipping_country",
                            length = 100
                    )
            )
    })
    private InvoiceAddress shippingAddress;

    @Column(
            name = "payment_method",
            length = 50
    )
    private String paymentMethod;

    /*
     * Financial snapshot.
     *
     * BigDecimal is used for monetary values.
     */
    @Column(
            name = "subtotal",
            nullable = false,
            precision = 19,
            scale = 2
    )
    private BigDecimal subtotal;

    @Column(
            name = "tax_amount",
            nullable = false,
            precision = 19,
            scale = 2
    )
    private BigDecimal taxAmount;

    @Column(
            name = "total_amount",
            nullable = false,
            precision = 19,
            scale = 2
    )
    private BigDecimal totalAmount;

    /*
     * Invoice line items.
     *
     * One invoice can contain multiple invoice items.
     */
    @OneToMany(
            mappedBy = "invoice",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    @Builder.Default
    private List<InvoiceItem> items = new ArrayList<>();

    @Column(
            name = "created_at",
            nullable = false
    )
    private Instant createdAt;

    @Column(
            name = "updated_at",
            nullable = false
    )
    private Instant updatedAt;
}