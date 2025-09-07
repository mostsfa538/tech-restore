package com.techRestore.tech.restore.admin.service;

import com.techRestore.tech.restore.common.model.entities.Product;
import com.techRestore.tech.restore.common.services.BaseService;
import com.techRestore.tech.restore.common.utils.DTOConverter;
import com.techRestore.tech.restore.shop.dto.product.ProductResponseDTO;
import com.techRestore.tech.restore.shop.dto.product.UpdateProductDto;
import com.techRestore.tech.restore.shop.repository.ProductRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class AdminProductService extends BaseService<Product, UUID> {

    public AdminProductService(ProductRepository productRepository) {
        super(productRepository);
    }

    public Page<ProductResponseDTO> getAllProducts(Pageable pageable) {
        return repository.findAll(pageable).map(DTOConverter::convertToProductDTO);
    }

    public ProductResponseDTO getProductById(UUID productId) {
        Product product = findByIdOrThrow(productId, "Product");
        return DTOConverter.convertToProductDTO(product);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public void updateProduct(UUID productId, UpdateProductDto updateProductDto) {
        Product product = findByIdOrThrow(productId, "Product");
        updateProductFields(product, updateProductDto);
        repository.save(product);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public void deleteProduct(UUID productId) {
        Product product = findByIdOrThrow(productId, "Product");
        product.setDeleted(true);
        repository.save(product);
    }

    private void updateProductFields(Product product, UpdateProductDto updateDto) {
        if (updateDto.name() != null && !updateDto.name().trim().isEmpty()) {
            product.setName(updateDto.name().trim());
        }
        if (updateDto.description() != null && !updateDto.description().trim().isEmpty()) {
            product.setDescription(updateDto.description().trim());
        }
        if (updateDto.price() != null) {
            product.setPrice(updateDto.price());
        }
        if (updateDto.imageUrl() != null) {
            product.setImageUrl(updateDto.imageUrl());
        }
        if (updateDto.category() != null) {
            product.setCategory(updateDto.category());
        }
        if (updateDto.stockQuantity() != null) {
            product.setStock(updateDto.stockQuantity());
        }
        if (updateDto.condition() != null) {
            product.setCondition(updateDto.condition());
        }
    }
}
