package com.techRestore.tech.restore.services.user;

import com.techRestore.tech.restore.dto.product.ProductResponseDTO;
import com.techRestore.tech.restore.exception.NotFoundException;
import com.techRestore.tech.restore.model.entities.Product;
import com.techRestore.tech.restore.repository.CategoryRepository;
import com.techRestore.tech.restore.repository.ProductRepository;

import org.hibernate.LazyInitializationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class UserServices {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductRepository productRepository;


    public List<ProductResponseDTO> getProductsByCategory(UUID shopId, UUID categoryId) {
        categoryRepository.findById(categoryId)
                .orElseThrow(() -> new NotFoundException("Category not Found"));

        return productRepository.findProductByCategoryId(shopId, categoryId).stream()
                .map(this::convertToDTO)
                .toList();
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
