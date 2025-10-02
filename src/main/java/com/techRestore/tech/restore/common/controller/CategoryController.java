package com.techRestore.tech.restore.common.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.techRestore.tech.restore.admin.dto.CategoryDTO;
import com.techRestore.tech.restore.common.services.category.CategoryServices;

import lombok.AllArgsConstructor;

@RestController
@RequestMapping("/api/categories")
@AllArgsConstructor
public class CategoryController {

    private final CategoryServices categoryService;

    @GetMapping
    public ResponseEntity<Page<CategoryDTO>> getAllCategories(Pageable pageable) {
        return ResponseEntity.ok(categoryService.getAllCategories(pageable));
    }
}
