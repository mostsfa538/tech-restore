package com.techRestore.tech.restore.common.services.category;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.techRestore.tech.restore.admin.dto.CategoryDTO;
import com.techRestore.tech.restore.admin.repository.CategoryRepository;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class CategoryServices {
    private final CategoryRepository categoryRepository;

    public Page<CategoryDTO> getAllCategories(Pageable pageable) {
        return categoryRepository.findAll(pageable)
                .map(category -> new CategoryDTO(category.getId(), category.getName()));
    }
}
