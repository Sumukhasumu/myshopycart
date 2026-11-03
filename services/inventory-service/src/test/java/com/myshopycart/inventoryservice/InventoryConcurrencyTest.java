package com.myshopycart.inventoryservice;

import com.myshopycart.inventoryservice.entity.Inventory;
import com.myshopycart.inventoryservice.exception.InsufficientStockException;
import com.myshopycart.inventoryservice.repository.InventoryRepository;
import com.myshopycart.inventoryservice.service.InventoryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class InventoryConcurrencyTest {

    @Autowired
    private InventoryRepository inventoryRepository;

    @Autowired
    private InventoryService inventoryService;

    @Test
    void shouldPreventOversellingDuringConcurrentReservations()
            throws Exception {

        String productId =
                "CONCURRENT-" + System.currentTimeMillis();

        Inventory inventory = new Inventory();

        inventory.setProductId(productId);
        inventory.setAvailableStock(10);
        inventory.setReservedStock(0);

        inventoryRepository.saveAndFlush(inventory);

        ExecutorService executor =
                Executors.newFixedThreadPool(2);

        CountDownLatch startLatch =
                new CountDownLatch(1);

        AtomicInteger successfulReservations =
                new AtomicInteger();

        AtomicInteger failedReservations =
                new AtomicInteger();

        Runnable reservationTask = () -> {

            try {

                startLatch.await();

                inventoryService.reserveStock(
                        productId,
                        7);

                successfulReservations.incrementAndGet();

            } catch (InsufficientStockException exception) {

                failedReservations.incrementAndGet();

            } catch (InterruptedException exception) {

                Thread.currentThread().interrupt();
            }
        };

        executor.submit(reservationTask);
        executor.submit(reservationTask);

        startLatch.countDown();

        executor.shutdown();

        boolean completed =
                executor.awaitTermination(
                        10,
                        TimeUnit.SECONDS);

        assertEquals(
                true,
                completed);

        assertEquals(
                1,
                successfulReservations.get());

        assertEquals(
                1,
                failedReservations.get());

        Inventory finalInventory =
                inventoryRepository
                        .findByProductId(productId)
                        .orElseThrow();

        assertEquals(
                3,
                finalInventory.getAvailableStock());

        assertEquals(
                7,
                finalInventory.getReservedStock());
    }
}