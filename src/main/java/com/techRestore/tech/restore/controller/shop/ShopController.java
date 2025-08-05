package com.techRestore.tech.restore.controller.shop;

import com.techRestore.tech.restore.dto.common.SearchRequest;
import com.techRestore.tech.restore.dto.shop.ShopResponseDto;
import com.techRestore.tech.restore.dto.shop.ShopUpdateRequest;
import com.techRestore.tech.restore.model.entities.Shop;
import com.techRestore.tech.restore.services.shop.ShopServices;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/shops")
public class ShopController {
    @Autowired
    private ShopServices shopServices;

    @GetMapping("")
    public ResponseEntity<List<ShopResponseDto>> getShops() {
        List<ShopResponseDto> shops = shopServices.getShops()
                .stream()
                .map(shopServices::toShopDto)
                .toList();

        return ResponseEntity.ok(shops);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ShopResponseDto> getShopById(@PathVariable UUID id) {
        ShopResponseDto shop = shopServices.getShopById(id);
        return ResponseEntity.ok(shop);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateShop(@PathVariable UUID id, @RequestBody ShopUpdateRequest shopUpdateRequest) {
        shopServices.updateShop(id, shopUpdateRequest);
        return ResponseEntity.ok().body("Updated Success");
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteShopById(@PathVariable UUID id) {
        shopServices.deleteShop(id);
        return ResponseEntity.ok().body("Removed Success");
    }

    @GetMapping("/search")
    public ResponseEntity<?> searchByName(@RequestBody SearchRequest searchRequest) {
        List<Shop> shop = shopServices.search(searchRequest.name());
         return  ResponseEntity.ok().body(shop);
    }
}
