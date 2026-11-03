package com.myshopycart.inventoryservice.controller;

import com.myshopycart.inventoryservice.dto.AdjustStockRequest;
import com.myshopycart.inventoryservice.entity.Inventory;
import com.myshopycart.inventoryservice.service.InventoryService;
import org.springframework.http.ResponseEntity;
import com.myshopycart.inventoryservice.entity.InventoryTransaction;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/inventory")
public class InventoryController {

    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @GetMapping("/my-inventory")
    public ResponseEntity<List<Inventory>> getMyInventory() {
        return ResponseEntity.ok(inventoryService.getMyInventory());
    }

    @PostMapping
    public ResponseEntity<Inventory> createInventory(
            @RequestBody Map<String, Object> request) {

        String productId = (String) request.get("productId");
        Integer availableStock =
                ((Number) request.get("availableStock")).intValue();

        Inventory inventory = inventoryService.createInventory(
                productId, availableStock);

        return ResponseEntity.ok(inventory);
    }

    @GetMapping("/{productId}")
    public ResponseEntity<Inventory> getInventory(
            @PathVariable String productId) {

        return ResponseEntity.ok(
                inventoryService.getInventoryForCurrentUser(productId));
    }

    @PostMapping("/{productId}/stock")
    public ResponseEntity<Inventory> addStock(
            @PathVariable String productId,
            @RequestBody Map<String, Object> request) {

        Integer quantity = ((Number) request.get("quantity")).intValue();

        return ResponseEntity.ok(
                inventoryService.addStock(productId, quantity));
    }

    @PostMapping("/{productId}/adjust")
    public ResponseEntity<Inventory> adjustStock(
            @PathVariable String productId,
            @RequestBody AdjustStockRequest request) {

        Inventory inventory = inventoryService.adjustStock(
                productId,
                request.quantity(),
                request.operation(),
                request.reason());

        return ResponseEntity.ok(inventory);
    }

    @GetMapping("/{productId}/history")
    public ResponseEntity<List<InventoryTransaction>> getTransactionHistory(
            @PathVariable String productId) {

        return ResponseEntity.ok(
                inventoryService.getTransactionHistory(productId));
    }

    @GetMapping("/{productId}/availability")
    public ResponseEntity<Boolean> checkAvailability(
            @PathVariable String productId,
            @RequestParam Integer quantity) {

        return ResponseEntity.ok(
                inventoryService.isStockAvailable(productId, quantity));
    }

    @PostMapping("/{productId}/reserve")
    public ResponseEntity<Inventory> reserveStock(
            @PathVariable String productId,
            @RequestBody Map<String, Object> request) {

        Integer quantity = ((Number) request.get("quantity")).intValue();

        return ResponseEntity.ok(
                inventoryService.reserveStock(productId, quantity));
    }

    @PostMapping("/{productId}/release")
    public ResponseEntity<Inventory> releaseStock(
            @PathVariable String productId,
            @RequestBody Map<String, Object> request) {

        Integer quantity = ((Number) request.get("quantity")).intValue();

        return ResponseEntity.ok(
                inventoryService.releaseStock(productId, quantity));
    }

    @PostMapping("/{productId}/commit")
    public ResponseEntity<Inventory> commitStock(
            @PathVariable String productId,
            @RequestBody Map<String, Object> request) {

        Integer quantity = ((Number) request.get("quantity")).intValue();

        return ResponseEntity.ok(
                inventoryService.commitStock(productId, quantity));
    }
}