package com.techRestore.tech.restore.user.controller;

import java.util.UUID;

import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.techRestore.tech.restore.common.controller.BaseController;
import com.techRestore.tech.restore.common.dto.address.AddressRequest;
import com.techRestore.tech.restore.common.dto.address.AddressResponse;
import com.techRestore.tech.restore.common.services.address.AddressService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/users/addresses")
@RequiredArgsConstructor
public class AddressController extends BaseController {

    private final AddressService addressService;

    @PostMapping
    public ResponseEntity<AddressResponse> addAddress(@RequestBody @Valid AddressRequest request) {
        AddressResponse response = addressService.addAddress(request);
        return createdResponse(response);
    }

    @PutMapping("/{addressId}")
    public ResponseEntity<AddressResponse> updateAddress(@PathVariable UUID addressId,
            @RequestBody @Valid AddressRequest request) {
        AddressResponse response = addressService.updateAddress(addressId, request);
        return updatedResponse(response);
    }

    @DeleteMapping("/{addressId}")
    public ResponseEntity<Void> deleteAddress(@PathVariable UUID addressId) {
        addressService.deleteAddress(addressId);
        return deletedResponse();
    }

    @GetMapping
    public ResponseEntity<Page<AddressResponse>> getAddresses(Pageable pageable) {
        Page<AddressResponse> addresses = addressService.getUserAddresses(pageable);
        return successResponse(addresses);
    }

}
