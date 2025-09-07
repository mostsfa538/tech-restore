package com.techRestore.tech.restore.admin.controller;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.techRestore.tech.restore.admin.service.AdminProductService;
import com.techRestore.tech.restore.common.controller.BaseController;
import com.techRestore.tech.restore.shop.dto.product.ProductResponseDTO;
import com.techRestore.tech.restore.shop.dto.product.UpdateProductDto;

@RestController
@RequestMapping("/api/admin/products")
public class AdminProductController extends BaseController {
    private final AdminProductService productServices;

    public AdminProductController(AdminProductService productServices) {
        this.productServices = productServices;
    }

    @GetMapping
    public ResponseEntity<Page<ProductResponseDTO>> getAllProducts(Pageable pageable) {
        return successResponse(productServices.getAllProducts(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getProductById(@PathVariable UUID id) {
        return successResponse(productServices.getProductById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> updateProduct(
            @PathVariable UUID id,
            @RequestBody UpdateProductDto updateProductDto) {
        productServices.updateProduct(id, updateProductDto);
        return updatedResponse();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable UUID id) {
        productServices.deleteProduct(id);
        return deletedResponse();
    }
}
