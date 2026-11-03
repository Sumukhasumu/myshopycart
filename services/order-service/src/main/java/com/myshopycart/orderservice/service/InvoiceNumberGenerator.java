package com.myshopycart.orderservice.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Year;

@Component
public class InvoiceNumberGenerator {

    private static final String INVOICE_NUMBER_SEQUENCE =
            "invoice_number_seq";

    private static final String INVOICE_NUMBER_PREFIX =
            "INV-";

    @PersistenceContext
    private EntityManager entityManager;

    @Transactional
    public String generate() {

        Number sequenceValue =
                (Number) entityManager
                        .createNativeQuery(
                                "SELECT nextval('"
                                        + INVOICE_NUMBER_SEQUENCE
                                        + "')")
                        .getSingleResult();

        int currentYear =
                Year.now().getValue();

        return String.format(
                "%s%d-%06d",
                INVOICE_NUMBER_PREFIX,
                currentYear,
                sequenceValue.longValue()
        );
    }
}