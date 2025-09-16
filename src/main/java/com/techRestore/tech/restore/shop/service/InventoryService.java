package com.techRestore.tech.restore.shop.service;

import java.math.BigDecimal;
import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import java.io.*;
import java.util.*;

import com.techRestore.tech.restore.common.exception.NotFoundException;
import com.techRestore.tech.restore.common.model.entities.Product;
import com.techRestore.tech.restore.common.model.entities.Shop;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.techRestore.tech.restore.common.utils.DTOConverter;
import com.techRestore.tech.restore.shop.dto.product.ProductResponseDTO;
import com.techRestore.tech.restore.shop.repository.ProductRepository;
import com.techRestore.tech.restore.shop.repository.ShopRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class InventoryService {

    private final ProductRepository productRepository;
    private final ShopRepository shopRepository;
    private final int LOW_STOCK_THRESHOLD = 5;
    private final int OUT_OF_STOCK_THRESHOLD = 0;

    private UUID getCurrentShop() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        Shop shop = shopRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("Shop not found"));
        return shop.getId();
    }

    public Page<ProductResponseDTO> searchInventory(String query, Pageable pageable) {
        UUID shopId = getCurrentShop();
        return productRepository.searchByKeywordByShopId(shopId, query, pageable).map(
                DTOConverter::convertToProductDTO);
    }

    public Page<ProductResponseDTO> getLowStockProducts(Pageable pageable) {
        UUID shopId = getCurrentShop();
        return productRepository.findByStockLessThanEqual(shopId, LOW_STOCK_THRESHOLD, pageable).map(
                DTOConverter::convertToProductDTO);
    }

    public Page<ProductResponseDTO> getOutOfStockProducts() {
        UUID shopId = getCurrentShop();
        return productRepository.findByStockLessThanEqual(shopId, OUT_OF_STOCK_THRESHOLD, Pageable.unpaged()).map(
                DTOConverter::convertToProductDTO);
    }

    public BigDecimal getTotalInventoryValue() {
        UUID shopId = getCurrentShop();
        return productRepository.findAllByShopId(shopId).stream()
                .map(p -> {
                    BigDecimal price = p.getPrice() != null ? p.getPrice() : BigDecimal.ZERO;
                    BigDecimal qty = p.getStock() != null ? BigDecimal.valueOf(p.getStock()) : BigDecimal.ZERO;
                    return price.multiply(qty);
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public Long getTotalItemsInInventory() {
        UUID shopId = getCurrentShop();
        return productRepository.findAllByShopId(shopId).stream()
                .mapToLong(p -> p.getStock())
                .sum();
    }

    // public void importInventoryData(String filePath) {
    // UUID shopId = getCurrentShop();
    // try (CSVReader reader = new CSVReader(new FileReader(filePath))) {
    // List<Product> products = new ArrayList<>();
    // String[] nextLine;
    // reader.readNext();
    // while ((nextLine = reader.readNext()) != null) {
    // Product product = new Product();
    // product.setName(nextLine[0]);
    // product.setDescription(nextLine[1]);
    // product.setPrice(new BigDecimal(nextLine[2]));
    // product.setStock(Integer.parseInt(nextLine[3]));
    // product.setImageUrl(nextLine[4]);
    // product.setShopId(shopId);

    // products.add(product);
    // }
    // productRepository.saveAll(products);
    // } catch (Exception e) {
    // e.printStackTrace();
    // }
    // }

    public byte[] exportInventoryData() {
        UUID shopId = getCurrentShop();
        try (ByteArrayOutputStream out = new ByteArrayOutputStream();
                CSVWriter writer = new CSVWriter(new OutputStreamWriter(out))) {
            String[] header = { "Name", "Description", "Price", "Stock", "ImageUrl" };
            writer.writeNext(header);
            productRepository.findAllByShopId(shopId).forEach(product -> {
                String[] data = {
                        product.getName(),
                        product.getDescription(),
                        product.getPrice().toString(),
                        product.getStock().toString(),
                        product.getImageUrl()
                };
                writer.writeNext(data);
            });
            writer.flush();
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Failed to export inventory data", e);
        }
    }
}
