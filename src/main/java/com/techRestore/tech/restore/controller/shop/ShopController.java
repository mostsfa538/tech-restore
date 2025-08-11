package com.techRestore.tech.restore.controller.shop;

import com.techRestore.tech.restore.dto.shop.ShopUpdateRequest;
import com.techRestore.tech.restore.services.shop.ShopServices;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


import java.util.UUID;

@RestController
@RequestMapping("/api/shops")
public class ShopController {
    @Autowired
    private ShopServices shopServices;

    @PutMapping("/{id}")
    public ResponseEntity<?> updateShop(@PathVariable UUID id, @RequestBody ShopUpdateRequest shopUpdateRequest) {
        shopServices.updateShop(id, shopUpdateRequest);
        return ResponseEntity.ok().body("Updated Success");
    }
}
