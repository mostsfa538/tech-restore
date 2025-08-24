package com.techRestore.tech.restore.controller.shop;

import com.techRestore.tech.restore.controller.BaseController;
import com.techRestore.tech.restore.dto.common.address.AddressRequest;
import com.techRestore.tech.restore.dto.common.address.AddressResponse;
import com.techRestore.tech.restore.dto.common.address.AddressUpdate;
import com.techRestore.tech.restore.dto.shop.ShopResponseDto;
import com.techRestore.tech.restore.dto.shop.ShopUpdateRequest;
import com.techRestore.tech.restore.services.shop.ShopServices;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/shops")
public class ShopController extends BaseController {
    @Autowired
    private ShopServices shopServices;

    @PutMapping("/{id}")
    public ResponseEntity<ShopResponseDto> updateShop(@RequestBody ShopUpdateRequest shopUpdateRequest) {
        return updatedResponse(shopServices.updateShop(shopUpdateRequest));
    }

    @GetMapping("/address")
    public ResponseEntity<Page<AddressResponse>> getALlAddress(Pageable pageable) {
        return successResponse(shopServices.getAllAddresses(pageable));
    }

    @PostMapping("/address")
    public ResponseEntity<String> addAddress(@RequestBody AddressRequest addressRequest) {
        shopServices.addAddress(addressRequest);
        return createdResponse("Created successfully");
    }

    @PutMapping("/address/{id}")
    public ResponseEntity<String> updateAddress(@PathVariable UUID id, @RequestBody AddressUpdate addressUpdate) {
        shopServices.updateAddress(id, addressUpdate);
        return updatedResponse("Updated successfully");
    }

    @DeleteMapping("/address/{id}")
    public ResponseEntity<Void> deleteAddress(@PathVariable UUID id) {
        shopServices.deleteAddress(id);
        return deletedResponse();
    }
}
