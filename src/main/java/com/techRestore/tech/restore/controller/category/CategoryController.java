package com.techRestore.tech.restore.controller.category;

import com.techRestore.tech.restore.controller.BaseController;
import com.techRestore.tech.restore.dto.category.CategoryDTO;
import com.techRestore.tech.restore.services.category.CategoryServices;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/admin/categories")
public class CategoryController extends BaseController {
    @Autowired
    private CategoryServices categoryServices;

    @GetMapping
    public ResponseEntity<Page<CategoryDTO>> getAllCategories(Pageable pageable) {
        return successResponse(categoryServices.getAllCategories(pageable));
    }

    @PostMapping
    public ResponseEntity<Void> addCategory(@RequestBody CategoryDTO categoryDTO) {
        categoryServices.addCategory(categoryDTO);
        return updatedResponse();
    }

    @PutMapping("{id}")
    public ResponseEntity<Void> updateCategory(@PathVariable UUID id, @RequestBody CategoryDTO categoryDTO) {
        categoryServices.updateCategory(id, categoryDTO);
        return updatedResponse();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> removeCategory(@PathVariable UUID id) {
        categoryServices.removeCategory(id);
        return successMessageResponse("Removed success");
    }
}
