package com.myshopycart.cartservice.entity;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(
        name = "wishlist_item",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_wishlist_item_product",
                        columnNames = {"wishlist_id", "product_id"}
                )
        }
)
public class WishlistItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "wishlist_id",
            nullable = false
    )
    private Wishlist wishlist;

    @Column(name = "product_id", nullable = false)
    private String productId;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    public WishlistItem() {
    }

    public WishlistItem(
            Long id,
            Wishlist wishlist,
            String productId,
            Instant createdAt) {

        this.id = id;
        this.wishlist = wishlist;
        this.productId = productId;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Wishlist getWishlist() {
        return wishlist;
    }

    public void setWishlist(Wishlist wishlist) {
        this.wishlist = wishlist;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}