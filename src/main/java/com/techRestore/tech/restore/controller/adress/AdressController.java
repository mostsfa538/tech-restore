package com.techRestore.tech.restore.controller.adress;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.techRestore.tech.restore.controller.BaseController;
import com.techRestore.tech.restore.dto.common.address.AddressRequest;
import com.techRestore.tech.restore.dto.common.address.AddressResponse;
import com.techRestore.tech.restore.model.entities.User;
import com.techRestore.tech.restore.repository.UserRepository;
import com.techRestore.tech.restore.services.adress.AdressService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/users/addresses")
@RequiredArgsConstructor
public class AdressController extends BaseController {

    private final AdressService addressService;
    private final UserRepository userRepository;

    private UUID getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("No authenticated user found");
        }

        String email = authentication.getName();
        User user = userRepository.findByEmail(email);

        if (user == null || !user.isActivate()) {
            throw new RuntimeException("User account is deactivated or not found: " + email);
        }

        return user.getId();
    }

    @PostMapping
    public ResponseEntity<AddressResponse> addAddress(@RequestBody AddressRequest request) {
        UUID userId = getCurrentUserId();
        AddressResponse response = addressService.addAdress(userId, request);
        return createdResponse(response);
    }

    @PutMapping("/{addressId}")
    public ResponseEntity<AddressResponse> updateAddress(@PathVariable UUID addressId,
            @RequestBody AddressRequest request) {
        UUID userId = getCurrentUserId();
        AddressResponse response = addressService.updateAddress(userId, addressId, request);
        return updatedResponse(response);
    }

    @DeleteMapping("/{addressId}")
    public ResponseEntity<Void> deleteAddress(@PathVariable UUID addressId) {
        UUID userId = getCurrentUserId();
        addressService.deleteAddress(userId, addressId);
        return deletedResponse();
    }

    @GetMapping
    public ResponseEntity<Page<AddressResponse>> getAddresses(Pageable pageable) {
        UUID userId = getCurrentUserId();
        Page<AddressResponse> addresses = addressService.getUserAddresses(userId, pageable);
        return successResponse(addresses);
    }

}
