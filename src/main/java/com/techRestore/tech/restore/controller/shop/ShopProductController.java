package com.techRestore.tech.restore.controller.shop;

import com.techRestore.tech.restore.controller.BaseController;
import com.techRestore.tech.restore.dto.product.CreateProductDto;
import com.techRestore.tech.restore.dto.product.ProductResponseDTO;
import com.techRestore.tech.restore.dto.product.UpdateProductDto;
import com.techRestore.tech.restore.dto.shop.StockUpdateRequest;
import com.techRestore.tech.restore.services.shop.ShopProductService;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping(value = "/api/shops/products")
@AllArgsConstructor
public class ShopProductController extends BaseController {

    private ShopProductService shopProductService;

    @GetMapping
    public ResponseEntity<Page<ProductResponseDTO>> getProductsByCurrentShop(Pageable pageable) {
        Page<ProductResponseDTO> products = shopProductService.getProductsByShopId(pageable);
        return successResponse(products);
    }

    @PostMapping
    public ResponseEntity<ProductResponseDTO> addProductToShop(
            @RequestBody @Valid CreateProductDto createProductDto) {
        ProductResponseDTO product = shopProductService.addProductToShop(createProductDto);
        return createdResponse(product);
    }

    @PutMapping("/{productId}")
    public ResponseEntity<ProductResponseDTO> updateProduct(
            @PathVariable UUID productId,
            @RequestBody @Valid UpdateProductDto updateProductDto) {
        return updatedResponse(shopProductService.updateProduct(productId, updateProductDto));
    }

    @DeleteMapping("/{productId}")
    public ResponseEntity<Void> deleteProduct(@PathVariable UUID productId) {
        shopProductService.deleteProduct(productId);
        return deletedResponse();
    }

    @PatchMapping("/{productId}/stock")
    public ResponseEntity<ProductResponseDTO> updateProductStock(
            @PathVariable UUID productId,
            @RequestBody @Valid StockUpdateRequest request) {
        return updatedResponse(shopProductService.updateProductStock(productId, request));
    }

}
