package com.techRestore.tech.restore.shop.service;

import java.math.BigDecimal;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.techRestore.tech.restore.common.utils.DTOConverter;
import com.techRestore.tech.restore.shop.dto.product.ProductResponseDTO;
import com.techRestore.tech.restore.shop.repository.ProductRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class InventoryService {

    private final ProductRepository productRepository;
    private final int LOW_STOCK_THRESHOLD = 5;
    private final int OUT_OF_STOCK_THRESHOLD = 0;

    public Page<ProductResponseDTO> searchInventory(String query, Pageable pageable) {
        return productRepository.searchByKeyword(query, pageable).map(
                DTOConverter::convertToProductDTO);
    }

    public Page<ProductResponseDTO> getLowStockProducts(Pageable pageable) {
        return productRepository.findByStockLessThanEqual(LOW_STOCK_THRESHOLD, pageable).map(
                DTOConverter::convertToProductDTO);
    }

    public Page<ProductResponseDTO> getOutOfStockProducts() {
        return productRepository.findByStockLessThanEqual(OUT_OF_STOCK_THRESHOLD, Pageable.unpaged()).map(
                DTOConverter::convertToProductDTO);
    }

    public BigDecimal getTotalInventoryValue() {
        return productRepository.findAll().stream()
                .map(p -> p.getPrice().multiply(BigDecimal.valueOf(p.getStock())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public Long getTotalItemsInInventory() {
        return productRepository.findAll().stream()
                .mapToLong(p -> p.getStock())
                .sum();
    }

    public void importInventoryData(String filePath) {
        System.out.println("Importing inventory data from: " + filePath);
    }

    public void exportInventoryData(String filePath) {
        System.out.println("Exporting inventory data to: " + filePath);
    }
}
