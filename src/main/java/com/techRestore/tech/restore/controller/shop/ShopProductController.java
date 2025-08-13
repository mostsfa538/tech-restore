package com.techRestore.tech.restore.controller.shop;

import com.techRestore.tech.restore.dto.product.CreateProductDto;
import com.techRestore.tech.restore.dto.product.ProductResponseDTO;
import com.techRestore.tech.restore.dto.product.UpdateProductDto;
import com.techRestore.tech.restore.dto.shop.StockUpdateRequest;
import com.techRestore.tech.restore.services.shop.ShopProductService;

import lombok.AllArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(value = "/api/shops/products")
@AllArgsConstructor
public class ShopProductController {

    private ShopProductService shopProductService;

    @GetMapping
    public ResponseEntity<List<ProductResponseDTO>> getProductsByCurrentShop() {
        List<ProductResponseDTO> products = shopProductService.getProductsByShopId();
        return ResponseEntity.ok(products);
    }

    @PostMapping
    public ResponseEntity<ProductResponseDTO> addProductToShop(
            @RequestBody CreateProductDto createProductDto) {
        ProductResponseDTO product = shopProductService.addProductToShop(createProductDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(product);
    }

    @PutMapping("/{productId}")
    public ResponseEntity<ProductResponseDTO> updateProduct(
            @PathVariable UUID productId,
            @RequestBody UpdateProductDto updateProductDto) {
        return ResponseEntity.ok().body(shopProductService.updateProduct(productId, updateProductDto));
    }

    @DeleteMapping("/{productId}")
    public ResponseEntity<Void> deleteProduct(@PathVariable UUID productId) {
        shopProductService.deleteProduct(productId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{productId}/stock")
    public ResponseEntity<ProductResponseDTO> updateProductStock(
            @PathVariable UUID productId,
            @RequestBody StockUpdateRequest request) {
        return ResponseEntity.ok().body(shopProductService.updateProductStock(productId, request));
    }

}
