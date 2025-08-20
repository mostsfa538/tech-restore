package com.techRestore.tech.restore.services.product;

import com.techRestore.tech.restore.dto.product.ProductResponseDTO;
import com.techRestore.tech.restore.dto.product.UpdateProductDto;
import com.techRestore.tech.restore.exception.NotFoundException;
import com.techRestore.tech.restore.model.entities.Product;
import com.techRestore.tech.restore.repository.CategoryRepository;
import com.techRestore.tech.restore.repository.ProductRepository;
import com.techRestore.tech.restore.services.BaseService;
import com.techRestore.tech.restore.utils.DTOConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;

@Service
public class ProductServices extends BaseService<Product, UUID> {
    
    @Autowired
    private CategoryRepository categoryRepository;

    public ProductServices(ProductRepository productRepository) {
        super(productRepository);
    }

    public Page<ProductResponseDTO> getAllProducts(Pageable pageable) {
        return repository.findAll(pageable).map(DTOConverter::convertToProductDTO);
    }

    public ProductResponseDTO getProductById(UUID productId) {
        Product product = findByIdOrThrow(productId, "Product");
        return DTOConverter.convertToProductDTO(product);
    }

    public Page<ProductResponseDTO> searchProducts(String keyword, Pageable pageable) {
        return ((ProductRepository) repository).searchByKeyword(keyword, pageable)
            .map(DTOConverter::convertToProductDTO);
    }

    public Page<ProductResponseDTO> getProductsByPriceRange(BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable) {
        return ((ProductRepository) repository).findByPriceBetween(minPrice, maxPrice, pageable)
        .map(DTOConverter::convertToProductDTO);
    }

    public Page<ProductResponseDTO> getProductsByCategory(UUID categoryId, Pageable pageable) {
        categoryRepository.findById(categoryId)
                .orElseThrow(() -> new NotFoundException("Category not Found"));

        return ((ProductRepository) repository).findByCategoryId(categoryId, pageable)
                .map(DTOConverter::convertToProductDTO);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public void updateProduct(UUID productId, UpdateProductDto updateProductDto) {
        Product product = findByIdOrThrow(productId, "Product");
        updateProductFields(product, updateProductDto);
        repository.save(product);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public void deleteProduct(UUID productId) {
        deleteByIdOrThrow(productId, "Product");
    }

    private void updateProductFields(Product product, UpdateProductDto updateDto) {
        if (updateDto.name().trim() != null) {
            product.setName(updateDto.name().trim());
        }
        if (updateDto.description().trim() != null) {
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

        repository.save(product);
    }
}
