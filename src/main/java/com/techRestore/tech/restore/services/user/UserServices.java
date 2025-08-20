package com.techRestore.tech.restore.services.user;

import com.techRestore.tech.restore.dto.product.ProductResponseDTO;
import com.techRestore.tech.restore.exception.NotFoundException;
import com.techRestore.tech.restore.repository.CategoryRepository;
import com.techRestore.tech.restore.repository.ProductRepository;
import com.techRestore.tech.restore.utils.DTOConverter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class UserServices {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductRepository productRepository;

    public Page<ProductResponseDTO> getProductsByCategory(UUID shopId, UUID categoryId, Pageable pageable) {
        categoryRepository.findById(categoryId)
                .orElseThrow(() -> new NotFoundException("Category not Found"));

        return productRepository.findProductByCategoryId(shopId, categoryId, pageable)
                .map(DTOConverter::convertToProductDTO);
    }
}
