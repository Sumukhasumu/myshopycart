package com.myshopycart.inventoryservice;

import com.myshopycart.inventoryservice.entity.Inventory;
import com.myshopycart.inventoryservice.repository.InventoryRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class InventoryPessimisticLockingTest {

    @Autowired
    private InventoryRepository inventoryRepository;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Test
    void shouldLockInventoryRowDuringTransaction()
            throws Exception {

        Inventory inventory = new Inventory();

        inventory.setProductId(
                "PESS-" + System.currentTimeMillis());

        inventory.setAvailableStock(10);
        inventory.setReservedStock(0);

        Inventory savedInventory =
                inventoryRepository.saveAndFlush(inventory);

        Long inventoryId = savedInventory.getId();

        CountDownLatch lockAcquired =
                new CountDownLatch(1);

        CountDownLatch releaseTransaction =
                new CountDownLatch(1);

        Thread firstTransaction =
                new Thread(() -> {

                    TransactionTemplate transactionTemplate =
                            new TransactionTemplate(
                                    transactionManager);

                    transactionTemplate.executeWithoutResult(
                            status -> {

                                Inventory lockedInventory =
                                        inventoryRepository
                                                .findWithLockByProductId(
                                                        savedInventory
                                                                .getProductId())
                                                .orElseThrow();

                                assertEquals(
                                        inventoryId,
                                        lockedInventory.getId());

                                lockAcquired.countDown();

                                try {
                                    releaseTransaction.await(
                                            5,
                                            TimeUnit.SECONDS);
                                } catch (InterruptedException e) {
                                    Thread.currentThread()
                                            .interrupt();
                                }
                            });
                });

        firstTransaction.start();

        assertTrue(
                lockAcquired.await(
                        5,
                        TimeUnit.SECONDS));

        long startTime =
                System.currentTimeMillis();

        Thread secondTransaction =
                new Thread(() -> {

                    TransactionTemplate transactionTemplate =
                            new TransactionTemplate(
                                    transactionManager);

                    transactionTemplate.executeWithoutResult(
                            status -> {

                                inventoryRepository
                                        .findWithLockByProductId(
                                                savedInventory
                                                        .getProductId())
                                        .orElseThrow();
                            });
                });

        secondTransaction.start();

        Thread.sleep(500);

        assertTrue(
                secondTransaction.isAlive(),
                "Second transaction should wait for the pessimistic lock");

        releaseTransaction.countDown();

        firstTransaction.join(5000);
        secondTransaction.join(5000);

        long elapsedTime =
                System.currentTimeMillis() - startTime;

        assertTrue(
                elapsedTime >= 400,
                "Second transaction should have waited for the lock");
    }
}