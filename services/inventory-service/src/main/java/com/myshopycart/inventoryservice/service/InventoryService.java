package com.myshopycart.inventoryservice.service;

import com.myshopycart.inventoryservice.entity.Inventory;
import com.myshopycart.inventoryservice.entity.InventoryTransaction;
import com.myshopycart.inventoryservice.exception.InsufficientStockException;
import com.myshopycart.inventoryservice.exception.InvalidQuantityException;
import com.myshopycart.inventoryservice.exception.InventoryNotFoundException;
import com.myshopycart.inventoryservice.repository.InventoryRepository;
import com.myshopycart.inventoryservice.repository.InventoryTransactionRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.myshopycart.inventoryservice.entity.InventoryTransaction;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
public class InventoryService {

    private static final SimpleGrantedAuthority ADMIN_ROLE =
            new SimpleGrantedAuthority("ROLE_ADMIN");

    private static final SimpleGrantedAuthority DISTRIBUTOR_ROLE =
            new SimpleGrantedAuthority("ROLE_DISTRIBUTOR");

    private final InventoryRepository inventoryRepository;
    private final InventoryTransactionRepository inventoryTransactionRepository;
    private final ProductOwnershipClient productOwnershipClient;

    public InventoryService(
            InventoryRepository inventoryRepository,
            InventoryTransactionRepository inventoryTransactionRepository,
            ProductOwnershipClient productOwnershipClient) {
        this.inventoryRepository = inventoryRepository;
        this.inventoryTransactionRepository = inventoryTransactionRepository;
        this.productOwnershipClient = productOwnershipClient;
    }

    @Transactional
    public Inventory createInventory(String productId, Integer availableStock) {
        validateProductId(productId);
        validateQuantity(availableStock);
        verifyDistributorOwnsProduct(productId);

        if (inventoryRepository.findByProductId(productId).isPresent()) {
            throw new IllegalStateException(
                    "Inventory already exists for product: " + productId);
        }

        Inventory inventory = new Inventory();
        inventory.setProductId(productId);
        inventory.setAvailableStock(availableStock);
        inventory.setReservedStock(0);

        Instant now = Instant.now();
        inventory.setCreatedAt(now);
        inventory.setUpdatedAt(now);

        Inventory savedInventory = inventoryRepository.save(inventory);

        saveTransaction(productId, "INITIALIZE", availableStock,
                0, availableStock, "Inventory initialized");

        return savedInventory;
    }

    /**
     * Returns all inventory to administrators and only owned inventory
     * to distributors.
     */
    @Transactional(readOnly = true)
    public List<Inventory> getMyInventory() {
        Authentication authentication = getCurrentAuthentication();

        if (isAdmin(authentication)) {
            return inventoryRepository.findAll();
        }

        Long distributorId = getCurrentDistributorId(authentication);
        List<Inventory> ownedInventory = new ArrayList<>();

        for (Inventory inventory : inventoryRepository.findAll()) {
            try {
                if (productOwnershipClient.isOwnedByDistributor(
                        inventory.getProductId(), distributorId)) {
                    ownedInventory.add(inventory);
                }
            } catch (InventoryNotFoundException exception) {
                // A deleted product must not prevent other inventory
                // records from being displayed.
            }
        }

        return ownedInventory;
    }

    /**
     * Retrieves inventory for a product after verifying distributor
     * ownership. Administrators can access any product's inventory.
     */
    @Transactional(readOnly = true)
    public Inventory getInventoryForCurrentUser(String productId) {
        Authentication authentication = getCurrentAuthentication();

        if (isAdmin(authentication)) {
            return getInventoryByProductId(productId);
        }

        Long distributorId = getCurrentDistributorId(authentication);

        if (!productOwnershipClient.isOwnedByDistributor(productId, distributorId)) {
            throw new AccessDeniedException(
                    "You cannot view inventory for a product you do not own");
        }

        return getInventoryByProductId(productId);
    }

    /**
     * Internal inventory lookup used by stock operations and availability
     * checks. Controller access must use getInventoryForCurrentUser().
     */
    @Transactional(readOnly = true)
    public Inventory getInventoryByProductId(String productId) {
        return inventoryRepository.findByProductId(productId)
                .orElseThrow(() -> new InventoryNotFoundException(
                        "Inventory not found for product: " + productId));
    }

    @Transactional
    public Inventory addStock(String productId, Integer quantity) {
        validateProductId(productId);
        validateQuantity(quantity);
        verifyDistributorOwnsProduct(productId);

        Inventory inventory = getInventoryWithLock(productId);
        Integer previousStock = inventory.getAvailableStock();

        inventory.setAvailableStock(inventory.getAvailableStock() + quantity);
        inventory.setUpdatedAt(Instant.now());

        Inventory savedInventory = inventoryRepository.save(inventory);

        saveTransaction(productId, "ADD_STOCK", quantity,
                previousStock, savedInventory.getAvailableStock(), "Stock added");

        return savedInventory;
    }

    @Transactional(readOnly = true)
    public boolean isStockAvailable(String productId, Integer quantity) {
        validateProductId(productId);
        validateQuantity(quantity);

        Inventory inventory = getInventoryByProductId(productId);
        return inventory.getAvailableStock() >= quantity;
    }

    @Transactional
    public Inventory reserveStock(String productId, Integer quantity) {
        validateProductId(productId);
        validateQuantity(quantity);

        Inventory inventory = getInventoryWithLock(productId);

        if (inventory.getAvailableStock() < quantity) {
            throw new InsufficientStockException(
                    "Insufficient stock for product: " + productId);
        }

        Integer previousStock = inventory.getAvailableStock();

        inventory.setAvailableStock(inventory.getAvailableStock() - quantity);
        inventory.setReservedStock(inventory.getReservedStock() + quantity);
        inventory.setUpdatedAt(Instant.now());

        Inventory savedInventory = inventoryRepository.save(inventory);

        saveTransaction(productId, "RESERVE", quantity,
                previousStock, savedInventory.getAvailableStock(),
                "Stock reserved for order");

        return savedInventory;
    }

    @Transactional
    public Inventory releaseStock(String productId, Integer quantity) {
        validateProductId(productId);
        validateQuantity(quantity);

        Inventory inventory = getInventoryWithLock(productId);

        if (inventory.getReservedStock() < quantity) {
            throw new InsufficientStockException(
                    "Insufficient reserved stock for product: " + productId);
        }

        Integer previousStock = inventory.getAvailableStock();

        inventory.setReservedStock(inventory.getReservedStock() - quantity);
        inventory.setAvailableStock(inventory.getAvailableStock() + quantity);
        inventory.setUpdatedAt(Instant.now());

        Inventory savedInventory = inventoryRepository.save(inventory);

        saveTransaction(productId, "RELEASE", quantity,
                previousStock, savedInventory.getAvailableStock(),
                "Reserved stock released");

        return savedInventory;
    }

    @Transactional
    public Inventory commitStock(String productId, Integer quantity) {
        validateProductId(productId);
        validateQuantity(quantity);

        Inventory inventory = getInventoryWithLock(productId);

        if (inventory.getReservedStock() < quantity) {
            throw new InsufficientStockException(
                    "Insufficient reserved stock to commit for product: " + productId);
        }

        Integer previousStock = inventory.getAvailableStock();

        inventory.setReservedStock(inventory.getReservedStock() - quantity);
        inventory.setUpdatedAt(Instant.now());

        Inventory savedInventory = inventoryRepository.save(inventory);

        saveTransaction(productId, "COMMIT", quantity,
                previousStock, savedInventory.getAvailableStock(),
                "Reserved stock committed after payment");

        return savedInventory;
    }

    @Transactional
    public Inventory adjustStock(
            String productId, Integer quantity, String operation, String reason) {

        validateProductId(productId);
        validateQuantity(quantity);
        verifyDistributorOwnsProduct(productId);

        if (operation == null
                || (!"INCREASE".equals(operation) && !"DECREASE".equals(operation))) {
            throw new InvalidQuantityException(
                    "Operation must be INCREASE or DECREASE");
        }

        if (reason == null || reason.isBlank()) {
            throw new InvalidQuantityException(
                    "Adjustment reason must not be blank");
        }

        Inventory inventory = getInventoryWithLock(productId);
        Integer previousStock = inventory.getAvailableStock();

        if ("INCREASE".equals(operation)) {
            inventory.setAvailableStock(
                    Math.addExact(inventory.getAvailableStock(), quantity));
        } else {
            if (inventory.getAvailableStock() < quantity) {
                throw new InsufficientStockException(
                        "Adjustment would result in negative available stock");
            }

            inventory.setAvailableStock(inventory.getAvailableStock() - quantity);
        }

        inventory.setUpdatedAt(Instant.now());

        Inventory savedInventory = inventoryRepository.save(inventory);

        saveTransaction(productId, operation, quantity,
                previousStock, savedInventory.getAvailableStock(), reason);

        return savedInventory;
    }

    @Transactional(readOnly = true)
	public List<InventoryTransaction> getTransactionHistory(String productId) {
    		validateProductId(productId);
    		verifyDistributorOwnsProduct(productId);

    		// Confirm inventory exists before retrieving its history.
    		getInventoryByProductId(productId);

    		return inventoryTransactionRepository
            	.findByProductIdOrderByCreatedAtDesc(productId);
}

    private void saveTransaction(
            String productId,
            String transactionType,
            Integer quantity,
            Integer previousStock,
            Integer newStock,
            String reason) {

        InventoryTransaction transaction = new InventoryTransaction();
        transaction.setProductId(productId);
        transaction.setTransactionType(transactionType);
        transaction.setQuantity(quantity);
        transaction.setPreviousAvailableStock(previousStock);
        transaction.setNewAvailableStock(newStock);
        transaction.setReason(reason);

        inventoryTransactionRepository.save(transaction);
    }

    private Inventory getInventoryWithLock(String productId) {
        return inventoryRepository.findWithLockByProductId(productId)
                .orElseThrow(() -> new InventoryNotFoundException(
                        "Inventory not found for product: " + productId));
    }

    private void validateProductId(String productId) {
        if (productId == null || productId.isBlank()) {
            throw new IllegalArgumentException("Product ID must not be blank");
        }
    }

    private void validateQuantity(Integer quantity) {
        if (quantity == null || quantity <= 0) {
            throw new InvalidQuantityException(
                    "Quantity must be greater than zero");
        }
    }

    private Authentication getCurrentAuthentication() {
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AccessDeniedException("Authentication is required");
        }

        return authentication;
    }

    private boolean isAdmin(Authentication authentication) {
        return authentication.getAuthorities().contains(ADMIN_ROLE);
    }

    private Long getCurrentDistributorId(Authentication authentication) {
        if (!authentication.getAuthorities().contains(DISTRIBUTOR_ROLE)) {
            throw new AccessDeniedException(
                    "Only distributors and administrators can access inventory");
        }

        Object details = authentication.getDetails();

        if (!(details instanceof Long distributorId)) {
            throw new AccessDeniedException(
                    "Distributor identity could not be verified");
        }

        return distributorId;
    }

    private void verifyDistributorOwnsProduct(String productId) {
        Authentication authentication = getCurrentAuthentication();

        if (isAdmin(authentication)) {
            return;
        }

        Long distributorId = getCurrentDistributorId(authentication);

        if (!productOwnershipClient.isOwnedByDistributor(productId, distributorId)) {
            throw new AccessDeniedException(
                    "You cannot modify inventory for a product you do not own");
        }
    }
}