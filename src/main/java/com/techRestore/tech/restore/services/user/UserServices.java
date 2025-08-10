package com.techRestore.tech.restore.services.user;

import com.techRestore.tech.restore.dto.product.ProductResponseDTO;
import com.techRestore.tech.restore.exception.NotFoundException;
import com.techRestore.tech.restore.repository.CategoryRepository;
import com.techRestore.tech.restore.repository.ProductRepository;
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

        return productRepository.findProductDTOsByCategoryId(shopId, categoryId);
    }
}
