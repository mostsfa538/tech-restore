package com.techRestore.tech.restore.controller.shop;

import com.techRestore.tech.restore.dto.product.CreateProductDto;
import com.techRestore.tech.restore.dto.product.ProductResponseDTO;
import com.techRestore.tech.restore.dto.product.UpdateProductDto;
import com.techRestore.tech.restore.dto.shop.StockUpdateRequest;
import com.techRestore.tech.restore.model.entities.Shop;
import com.techRestore.tech.restore.repository.ShopRepository;
import com.techRestore.tech.restore.services.shop.ShopProductService;

import lombok.AllArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(value = "/api/shops/products")
@AllArgsConstructor
public class ShopProductController {

    private ShopProductService shopProductService;
    private final ShopRepository shopRepository;


    private UUID getCurrentShopId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
      String email = authentication.getName();
      Shop shop = shopRepository.findByEmail(email)
        .orElseThrow(() -> new UsernameNotFoundException("Shop not found"));

      if (shop == null) {
          throw new RuntimeException("User not found with email: " + email);
      }
      return shop.getId();
    }



    @GetMapping
    public ResponseEntity<List<ProductResponseDTO>> getProductsByCurrentShop() {
        UUID shopId = getCurrentShopId();
        List<ProductResponseDTO> products = shopProductService.getProductsByShopId(shopId);
        return ResponseEntity.ok(products);
    }

    @PostMapping
    public ResponseEntity<ProductResponseDTO> addProductToShop(
            @RequestBody CreateProductDto createProductDto) {
        UUID shopId = getCurrentShopId();
        ProductResponseDTO product = shopProductService.addProductToShop(shopId, createProductDto);
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
        UUID shopId = getCurrentShopId();
        return ResponseEntity.ok().body(shopProductService.updateProductStock(shopId, productId, request));
    }

}
