package com.techRestore.tech.restore.controller.shop;

import com.techRestore.tech.restore.dto.common.address.AddressRequest;
import com.techRestore.tech.restore.dto.common.address.AddressResponse;
import com.techRestore.tech.restore.dto.common.address.AddressUpdate;
import com.techRestore.tech.restore.dto.shop.ShopResponseDto;
import com.techRestore.tech.restore.dto.shop.ShopUpdateRequest;
import com.techRestore.tech.restore.services.shop.ShopServices;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/shops")
public class ShopController {
    @Autowired
    private ShopServices shopServices;

    @PutMapping("/{id}")
    public ResponseEntity<ShopResponseDto> updateShop(@RequestBody ShopUpdateRequest shopUpdateRequest) {
        return ResponseEntity.ok().body(shopServices.updateShop(shopUpdateRequest));
    }

    @GetMapping("/address")
    public ResponseEntity<List<AddressResponse>> getALlAddress() {
        return ResponseEntity.ok().body(shopServices.getAllAddresses());
    }

    @PostMapping("/address")
    public ResponseEntity<String> addAddress(@RequestBody AddressRequest addressRequest) {
        shopServices.addAddress(addressRequest);
        return ResponseEntity.ok().body("Created successfully");
    }

    @PutMapping("/address/{id}")
    public ResponseEntity<String> updateAddress(@PathVariable UUID id, @RequestBody AddressUpdate addressUpdate) {
        shopServices.updateAddress(id, addressUpdate);
        return ResponseEntity.ok().body("Updated successfully");
    }

    @DeleteMapping("/address/{id}")
    public ResponseEntity<String> deleteAddress(@PathVariable UUID id) {
        shopServices.deleteAddress(id);
        return ResponseEntity.ok().body("Removed successfully");
    }
}
