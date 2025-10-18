package com.techRestore.tech.restore.user.controller;

import com.techRestore.tech.restore.common.controller.BaseController;
import com.techRestore.tech.restore.shop.dto.product.ProductResponseDTO;
import com.techRestore.tech.restore.user.service.UserProductService;

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
    private UserProductService productServices;

    @GetMapping
    public ResponseEntity<Page<ProductResponseDTO>> getAllProducts(Pageable pageable) {
        return successResponse(productServices.getAllProducts(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponseDTO> getProductById(@PathVariable UUID id) {
        return successResponse(productServices.getProductById(id));
    }

    @GetMapping("/shop/{shopId}")
    public ResponseEntity<Page<ProductResponseDTO>> getProductByShopId(@PathVariable UUID shopId, Pageable pageable) {
        Page<ProductResponseDTO> products = productServices.getProductByShopId(shopId, pageable);
        return ResponseEntity.ok(products);
    }

    @GetMapping("/search")
    public ResponseEntity<Page<ProductResponseDTO>> searchProducts(@RequestParam String keyword, Pageable pageable) {
        Page<ProductResponseDTO> products = productServices.searchProducts(keyword, pageable);
        return successResponse(products);
    }

    @GetMapping("/category/{categoryId}")
    public ResponseEntity<Page<ProductResponseDTO>> getProductsByCategory(@PathVariable UUID categoryId,
            Pageable pageable) {
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

    @GetMapping("{shopId}/{categoryId}")
    public ResponseEntity<Page<ProductResponseDTO>> getProductsByCategory(@PathVariable UUID shopId,
            @PathVariable UUID categoryId, Pageable pageable) {
        Page<ProductResponseDTO> shops = productServices.getProductsByCategory(shopId, categoryId, pageable);
        return successResponse(shops);
    }
}
