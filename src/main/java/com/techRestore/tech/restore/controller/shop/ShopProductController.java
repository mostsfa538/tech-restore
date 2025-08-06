package com.techRestore.tech.restore.controller.shop;

import com.techRestore.tech.restore.dto.product.CreateProductDto;
import com.techRestore.tech.restore.dto.product.UpdateProductDto;
import com.techRestore.tech.restore.dto.shop.StockUpdateRequest;
import com.techRestore.tech.restore.model.entities.Product;
import com.techRestore.tech.restore.services.shop.ShopProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(value = "/api/shops/{shopId}/products")
public class ShopProductController {
    @Autowired
    private ShopProductService shopProductService;

    @GetMapping
    public ResponseEntity<List<Product>> getProductsByShopId(@PathVariable UUID shopId) {
        List<Product> products = shopProductService.getProductsByShopId(shopId);
        return ResponseEntity.ok(products);
    }

    @PostMapping
    public ResponseEntity<Product> addProductToShop(
            @PathVariable UUID shopId,
            @RequestBody CreateProductDto createProductDto) {
        Product product = shopProductService.addProductToShop(shopId, createProductDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(product);
    }

    @PutMapping("/{productId}")
    public ResponseEntity<Void> updateProduct(
            @PathVariable UUID shopId,
            @PathVariable UUID productId,
            @RequestBody UpdateProductDto updateProductDto) {
        shopProductService.updateProduct(productId, updateProductDto);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{productId}")
    public ResponseEntity<Void> deleteProduct(
            @PathVariable UUID shopId,
            @PathVariable UUID productId) {
        shopProductService.deleteProduct(productId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/category/{category}")
    public ResponseEntity<List<Product>> getShopProductsWithCategory(
            @PathVariable UUID shopId,
            @PathVariable String category) {
        List<Product> products = shopProductService.getShopProductsWithCategory(shopId, category);
        return ResponseEntity.ok(products);
    }

    @PatchMapping("/{productId}/stock")
    public ResponseEntity<Void> updateProductStock(
            @PathVariable UUID shopId,
            @PathVariable UUID productId,
            @RequestBody StockUpdateRequest request) {
        shopProductService.updateProductStock(shopId, productId, request);
        return ResponseEntity.ok().build();
    }
}
