package com.myshopycart.inventoryservice.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "inventory_transactions")
public class InventoryTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String productId;

    @Column(nullable = false)
    private String transactionType;

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false)
    private Integer previousAvailableStock;

    @Column(nullable = false)
    private Integer newAvailableStock;

    @Column(length = 500)
    private String reason;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }

    public Long getId() {
        return id;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(String transactionType) {
        this.transactionType = transactionType;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public Integer getPreviousAvailableStock() {
        return previousAvailableStock;
    }

    public void setPreviousAvailableStock(Integer previousAvailableStock) {
        this.previousAvailableStock = previousAvailableStock;
    }

    public Integer getNewAvailableStock() {
        return newAvailableStock;
    }

    public void setNewAvailableStock(Integer newAvailableStock) {
        this.newAvailableStock = newAvailableStock;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}