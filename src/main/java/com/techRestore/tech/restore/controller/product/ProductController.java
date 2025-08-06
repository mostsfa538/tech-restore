package com.techRestore.tech.restore.controller.product;

import com.techRestore.tech.restore.dto.product.UpdateProductDto;
import com.techRestore.tech.restore.model.entities.Product;
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
    public ResponseEntity<List<Product>> getAllProducts() {
        List<Product> products = productServices.getAllProducts();
        return ResponseEntity.ok(products);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Product> getProductById(@PathVariable UUID id) {
        Product product = productServices.getProductById(id);
        return ResponseEntity.ok(product);
    }

    // GET /api/products/search?keyword={keyword} - Search products
    @GetMapping("/search")
    public ResponseEntity<List<Product>> searchProducts(@RequestParam String keyword) {
        List<Product> products = productServices.searchProducts(keyword);
        return ResponseEntity.ok(products);
    }

    @GetMapping("/category/{categoryId}")
    public ResponseEntity<List<Product>> getProductsByCategory(@PathVariable UUID categoryId) {
        List<Product> products = productServices.getProductsByCategory(categoryId);
        return ResponseEntity.ok(products);
    }

    @GetMapping("/filter")
    public ResponseEntity<List<Product>> getProductsWithFilters(@RequestParam String category) {
        List<Product> products = productServices.getProductsWithFilters(category);
        return ResponseEntity.ok(products);
    }

    // GET /api/products/price-range?minPrice={min}&maxPrice={max} - Get products by price range
    @GetMapping("/price-range")
    public ResponseEntity<List<Product>> getProductsByPriceRange(
            @RequestParam BigDecimal minPrice,
            @RequestParam BigDecimal maxPrice) {
        List<Product> products = productServices.getProductsByPriceRange(minPrice, maxPrice);
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
