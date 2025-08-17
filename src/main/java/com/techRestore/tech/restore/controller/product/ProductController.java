package com.techRestore.tech.restore.controller.product;

import com.techRestore.tech.restore.dto.product.ProductResponseDTO;
import com.techRestore.tech.restore.dto.product.UpdateProductDto;
import com.techRestore.tech.restore.services.product.ProductServices;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/products")
public class ProductController {
    @Autowired
    private ProductServices productServices;

    @GetMapping
    public ResponseEntity<List<ProductResponseDTO>> getAllProducts() {
        return ResponseEntity.ok(productServices.getAllProducts());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponseDTO> getProductById(@PathVariable UUID id) {
        return ResponseEntity.ok(productServices.getProductById(id));
    }

    // GET /api/products/search?keyword={keyword} - Search products
    @GetMapping("/search")
    public ResponseEntity<List<ProductResponseDTO>> searchProducts(@RequestParam String keyword) {
        List<ProductResponseDTO> products = productServices.searchProducts(keyword);
        return ResponseEntity.ok(products);
    }

    @GetMapping("/category/{categoryId}")
    public ResponseEntity<List<ProductResponseDTO>> getProductsByCategory(@PathVariable UUID categoryId) {
        List<ProductResponseDTO> products = productServices.getProductsByCategory(categoryId);
        return ResponseEntity.ok(products);
    }


    // GET /api/products/price-range?minPrice={min}&maxPrice={max} - Get products by price range
    @GetMapping("/price-range")
    public ResponseEntity<List<ProductResponseDTO>> getProductsByPriceRange(
            @RequestParam BigDecimal minPrice,
            @RequestParam BigDecimal maxPrice) {
        List<ProductResponseDTO> products = productServices.getProductsByPriceRange(minPrice, maxPrice);
        return ResponseEntity.ok(products);
    }

    // Update product (Admin only)
    @PutMapping("/{id}")
    public ResponseEntity<Void> updateProduct(
            @PathVariable UUID id,
            @RequestBody UpdateProductDto updateProductDto) {
        productServices.updateProduct(id, updateProductDto);
        return ResponseEntity.ok().build();
    }

    // Delete product (Admin only)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable UUID id) {
        productServices.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }
}
