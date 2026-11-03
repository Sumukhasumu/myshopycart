package com.myshopycart.cartservice.repository;

import com.myshopycart.cartservice.entity.Cart;
import com.myshopycart.cartservice.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CartItemRepository
        extends JpaRepository<CartItem, Long> {

    Optional<CartItem> findByCartAndProductId(
            Cart cart,
            String productId);

    List<CartItem> findByCart(Cart cart);

    void deleteByCartAndProductId(
            Cart cart,
            String productId);
}