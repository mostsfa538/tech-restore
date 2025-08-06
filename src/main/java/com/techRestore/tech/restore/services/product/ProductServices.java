package com.techRestore.tech.restore.services.product;

import com.techRestore.tech.restore.dto.product.UpdateProductDto;
import com.techRestore.tech.restore.exception.NotFoundException;
import com.techRestore.tech.restore.model.entities.Product;
import com.techRestore.tech.restore.repository.ProductRepository;
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

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public Product getProductById(UUID productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new NotFoundException("Product not found with id: " + productId));
    }

    public List<Product> searchProducts(String keyword) {
        return productRepository.searchByKeyword(keyword);
    }

    public List<Product> getProductsByCategory(UUID categoryId) {
        return productRepository.findByCategoryId(categoryId);
    }

    public List<Product> getProductsWithFilters(String category) {
        return productRepository.findWithFilters(category);
    }


    public List<Product> getProductsByPriceRange(BigDecimal minPrice, BigDecimal maxPrice) {
        return productRepository.findByPriceBetween(minPrice, maxPrice);
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
}
