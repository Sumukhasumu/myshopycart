package com.myshopycart.orderservice.repository;

import com.myshopycart.orderservice.entity.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface InvoiceRepository
        extends JpaRepository<Invoice, Long> {

    Optional<Invoice> findByOrderId(Long orderId);

    Optional<Invoice> findByInvoiceNumber(String invoiceNumber);

    boolean existsByOrderId(Long orderId);

    boolean existsByInvoiceNumber(String invoiceNumber);
}