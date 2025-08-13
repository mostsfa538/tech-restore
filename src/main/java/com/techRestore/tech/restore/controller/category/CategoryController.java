package com.techRestore.tech.restore.controller.category;

import com.techRestore.tech.restore.dto.category.CategoryDTO;
import com.techRestore.tech.restore.services.category.CategoryServices;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/categories")
public class CategoryController {
    @Autowired
    private CategoryServices categoryServices;

    @GetMapping
    public ResponseEntity<List<CategoryDTO>> getAllCategories(){
        return ResponseEntity.ok().body(categoryServices.getAllCategories());
    }

    @PostMapping
    public ResponseEntity<Void> addCategory(@RequestBody CategoryDTO categoryDTO) {
        categoryServices.addCategory(categoryDTO);
        return ResponseEntity.ok().build();
    }

    @PutMapping("{id}")
    public ResponseEntity<Void> updateCategory(@PathVariable UUID id, @RequestBody CategoryDTO categoryDTO) {
        categoryServices.updateCategory(id, categoryDTO);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> removeCategory(@PathVariable UUID id) {
        categoryServices.removeCategory(id);
        return ResponseEntity.ok().body("Removed success");
    }

}
