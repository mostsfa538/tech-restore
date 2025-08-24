package com.techRestore.tech.restore.controller.shop.product;

import com.techRestore.tech.restore.controller.BaseController;
import com.techRestore.tech.restore.dto.product.ProductResponseDTO;
import com.techRestore.tech.restore.dto.product.UpdateProductDto;
import com.techRestore.tech.restore.services.product.ProductServices;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.UUID;

@RestController
@RequestMapping("/api/products")
public class ProductController extends BaseController {
    @Autowired
    private ProductServices productServices;

    @GetMapping
    public ResponseEntity<Page<ProductResponseDTO>> getAllProducts(Pageable pageable) {
        return successResponse(productServices.getAllProducts(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponseDTO> getProductById(@PathVariable UUID id) {
        return successResponse(productServices.getProductById(id));
    }

    @GetMapping("/search")
    public ResponseEntity<Page<ProductResponseDTO>> searchProducts(@RequestParam String keyword, Pageable pageable) {
        Page<ProductResponseDTO> products = productServices.searchProducts(keyword, pageable);
        return successResponse(products);
    }

    @GetMapping("/category/{categoryId}")
    public ResponseEntity<Page<ProductResponseDTO>> getProductsByCategory(@PathVariable UUID categoryId, Pageable pageable) {
        Page<ProductResponseDTO> products = productServices.getProductsByCategory(categoryId, pageable);
        return successResponse(products);
    }

    @GetMapping("/price-range")
    public ResponseEntity<Page<ProductResponseDTO>> getProductsByPriceRange(
            @RequestParam BigDecimal minPrice,
            @RequestParam BigDecimal maxPrice,
            Pageable pageable) {
        Page<ProductResponseDTO> products = productServices.getProductsByPriceRange(minPrice, maxPrice, pageable);
        return successResponse(products);
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
