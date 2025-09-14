package com.techRestore.tech.restore.shop.controller;

import java.math.BigDecimal;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.techRestore.tech.restore.shop.dto.product.ProductResponseDTO;
import com.techRestore.tech.restore.shop.service.InventoryService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/shop/inventory")
@RequiredArgsConstructor
public class InventoryContoller {

    private final InventoryService inventoryService;

    @GetMapping("/search")
    public ResponseEntity<Page<ProductResponseDTO>> searchInventory(
            @RequestParam String query,
            Pageable pageable) {
        return ResponseEntity.ok(inventoryService.searchInventory(query, pageable));
    }

    @GetMapping("/low-stock")
    public ResponseEntity<Page<ProductResponseDTO>> getLowStockProducts(Pageable pageable) {
        return ResponseEntity.ok(inventoryService.getLowStockProducts(pageable));
    }

    @GetMapping("/out-of-stock")
    public ResponseEntity<Page<ProductResponseDTO>> getOutOfStockProducts() {
        return ResponseEntity.ok(inventoryService.getOutOfStockProducts());
    }

    @GetMapping("/total-value")
    public ResponseEntity<BigDecimal> getTotalInventoryValue() {
        return ResponseEntity.ok(inventoryService.getTotalInventoryValue());
    }

    @GetMapping("/total-items")
    public ResponseEntity<Long> getTotalItemsInInventory() {
        return ResponseEntity.ok(inventoryService.getTotalItemsInInventory());
    }

    @PostMapping("/import")
    public ResponseEntity<Void> importInventoryData(@RequestParam("filePath") String filePath) {
        inventoryService.importInventoryData(filePath);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/export")
    public ResponseEntity<Void> exportInventoryData(@RequestParam("filePath") String filePath) {
        inventoryService.exportInventoryData(filePath);
        return ResponseEntity.ok().build();
    }

}
