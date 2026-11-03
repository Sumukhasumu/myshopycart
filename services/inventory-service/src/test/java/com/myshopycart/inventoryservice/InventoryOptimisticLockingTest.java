package com.myshopycart.inventoryservice;

import com.myshopycart.inventoryservice.entity.Inventory;
import com.myshopycart.inventoryservice.repository.InventoryRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.OptimisticLockException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class InventoryOptimisticLockingTest {

    @Autowired
    private InventoryRepository inventoryRepository;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Test
    void shouldDetectOptimisticLockConflict() {

        Inventory inventory = new Inventory();

        inventory.setProductId(
                "OPT-" + System.currentTimeMillis());

        inventory.setAvailableStock(10);
        inventory.setReservedStock(0);

        Inventory savedInventory =
                inventoryRepository.saveAndFlush(inventory);

        Long inventoryId = savedInventory.getId();

        Long initialVersion =
                savedInventory.getVersion();

        Inventory firstTransactionInventory =
                loadInventory(inventoryId);

        Inventory secondTransactionInventory =
                loadInventory(inventoryId);

        firstTransactionInventory.setAvailableStock(8);

        saveInTransaction(firstTransactionInventory);

        Inventory updatedInventory =
                inventoryRepository.findById(inventoryId)
                        .orElseThrow();

        assertEquals(
                initialVersion + 1,
                updatedInventory.getVersion());

        secondTransactionInventory.setAvailableStock(7);

        assertThrows(
                OptimisticLockException.class,
                () -> saveInTransaction(
                        secondTransactionInventory));
    }

    private Inventory loadInventory(Long inventoryId) {

        entityManager.clear();

        return inventoryRepository.findById(inventoryId)
                .orElseThrow();
    }

    private void saveInTransaction(Inventory inventory) {

        TransactionTemplate transactionTemplate =
                new TransactionTemplate(transactionManager);

        transactionTemplate.executeWithoutResult(
                status -> {

                    Inventory managedInventory =
                            entityManager.merge(inventory);

                    entityManager.flush();
                });
    }
}