package com.techRestore.tech.restore.controller.shop;

import com.techRestore.tech.restore.dto.product.CreateProductDto;
import com.techRestore.tech.restore.dto.product.UpdateProductDto;
import com.techRestore.tech.restore.dto.shop.StockUpdateRequest;
import com.techRestore.tech.restore.model.entities.Product;
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
    public ResponseEntity<List<Product>> getProductsByCurrentShop() {
        UUID shopId = getCurrentShopId();
        List<Product> products = shopProductService.getProductsByShopId(shopId);
        return ResponseEntity.ok(products);
    }

    @PostMapping
    public ResponseEntity<Product> addProductToShop(
            @RequestBody CreateProductDto createProductDto) {
        UUID shopId = getCurrentShopId();
        Product product = shopProductService.addProductToShop(shopId, createProductDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(product);
    }

    @PutMapping("/{productId}")
    public ResponseEntity<Void> updateProduct(
            @PathVariable UUID productId,
            @RequestBody UpdateProductDto updateProductDto) {
        shopProductService.updateProduct(productId, updateProductDto);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{productId}")
    public ResponseEntity<Void> deleteProduct(@PathVariable UUID productId) {
        shopProductService.deleteProduct(productId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/category/{category}")
    public ResponseEntity<List<Product>> getShopProductsWithCategory(@PathVariable String category) {
        UUID shopId = getCurrentShopId();
        List<Product> products = shopProductService.getShopProductsWithCategory(shopId, category);
        return ResponseEntity.ok(products);
    }

    @PatchMapping("/{productId}/stock")
    public ResponseEntity<Void> updateProductStock(
            @PathVariable UUID productId,
            @RequestBody StockUpdateRequest request) {
        UUID shopId = getCurrentShopId();
        shopProductService.updateProductStock(shopId, productId, request);
        return ResponseEntity.ok().build();
    }

}
