package com.myshopycart.orderservice.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;

    private String customerName;

    private String customerEmail;

    private String customerMobile;

    /*
     * Checkout address snapshot.
     *
     * Captured when the customer places the order so that
     * invoice details do not depend on later profile changes.
     */
    @Column(name = "customer_address", length = 500)
    private String customerAddress;

    @Column(name = "customer_city", length = 100)
    private String customerCity;

    @Column(name = "customer_state", length = 100)
    private String customerState;

    @Column(name = "customer_postal_code", length = 20)
    private String customerPostalCode;

    @Column(name = "customer_country", length = 100)
    private String customerCountry;

    private BigDecimal totalAmount;

    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    private Instant createdAt;

    private Instant updatedAt;

    @OneToMany(
            mappedBy = "order",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    @Builder.Default
    private List<OrderItem> items = new ArrayList<>();
}