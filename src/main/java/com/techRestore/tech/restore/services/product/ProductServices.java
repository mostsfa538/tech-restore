package com.techRestore.tech.restore.services.product;

import com.techRestore.tech.restore.dto.product.ProductResponseDTO;
import com.techRestore.tech.restore.dto.product.UpdateProductDto;
import com.techRestore.tech.restore.exception.NotFoundException;
import com.techRestore.tech.restore.model.entities.Product;
import com.techRestore.tech.restore.repository.CategoryRepository;
import com.techRestore.tech.restore.repository.ProductRepository;
import org.hibernate.LazyInitializationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
public class ProductServices {
    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public Product getProductById(UUID productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new NotFoundException("Product not found with id: " + productId));
    }

    public List<ProductResponseDTO> searchProducts(String keyword) {
        return productRepository.searchByKeyword(keyword);
    }

    public List<ProductResponseDTO> getProductsByPriceRange(BigDecimal minPrice, BigDecimal maxPrice) {
        return productRepository.findByPriceBetween(minPrice, maxPrice);
    }

    public List<ProductResponseDTO> getProductsByCategory(UUID categoryId) {
        categoryRepository.findById(categoryId)
                .orElseThrow(() -> new NotFoundException("Category not Found"));

        return productRepository.findByCategoryId(categoryId).stream()
                .map(this::convertToDTO)
                .toList();
    }

    @PreAuthorize("hasRole('ADMIN')")
    public void updateProduct(UUID productId, UpdateProductDto updateProductDto) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new NotFoundException("Product not found with id: " + productId));

        updateProductFields(product, updateProductDto);
        productRepository.save(product);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public void deleteProduct(UUID productId) {
        if (!productRepository.existsById(productId)) {
            throw new NotFoundException("Product not found with id: " + productId);
        }
        productRepository.deleteById(productId);
    }

    private void updateProductFields(Product product, UpdateProductDto updateDto) {
        if (updateDto.name() != null) {
            product.setName(updateDto.name());
        }
        if (updateDto.description() != null) {
            product.setDescription(updateDto.description());
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

        productRepository.save(product);
    }

    private ProductResponseDTO convertToDTO(Product product) {
        String categoryName = null;
        try {
            if (product.getCategory() != null) {
                categoryName = product.getCategory().getName();
            }
        } catch (LazyInitializationException e) {
            categoryName = null;
        }

        return new ProductResponseDTO(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getStock(),
                product.getImageUrl(),
                product.getCondition(),
                product.getCreatedAt(),
                categoryName
        );
    }
}
