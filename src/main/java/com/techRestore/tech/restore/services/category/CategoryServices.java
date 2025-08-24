package com.techRestore.tech.restore.services.category;

import com.techRestore.tech.restore.dto.category.CategoryDTO;
import com.techRestore.tech.restore.model.entities.Category;
import com.techRestore.tech.restore.repository.CategoryRepository;
import com.techRestore.tech.restore.services.BaseService;
import com.techRestore.tech.restore.utils.DTOConverter;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class CategoryServices extends BaseService<Category, UUID> {

    public CategoryServices(CategoryRepository categoryRepository) {
        super(categoryRepository);
    }

    public Page<CategoryDTO> getAllCategories(Pageable pageable) {
        return repository.findAll(pageable)
                .map(DTOConverter::convertToCategoryDTO);
    }

    public void addCategory(CategoryDTO categoryDTO) {
        Category category = new Category();
        category.setName(categoryDTO.name());
        repository.save(category);
    }

    public void updateCategory(UUID id, CategoryDTO categoryDTO) {
        Category category = findByIdOrThrow(id, "Category");
        category.setName(categoryDTO.name());
        repository.save(category);
    }

    public void removeCategory(UUID id) {
        deleteByIdOrThrow(id, "Category");
    }
}
