package com.techRestore.tech.restore.user.controller;

import com.techRestore.tech.restore.common.controller.BaseController;
import com.techRestore.tech.restore.shop.dto.offers.OfferResponseDTO;
import com.techRestore.tech.restore.shop.dto.product.ProductResponseDTO;
import com.techRestore.tech.restore.shop.dto.shop.ShopResponseDto;
import com.techRestore.tech.restore.user.dto.user.UserProfileDTO;
import com.techRestore.tech.restore.user.dto.user.UserProfileUpdateDTO;
import com.techRestore.tech.restore.user.service.user.UserServices;

import lombok.RequiredArgsConstructor;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController extends BaseController {

    private final UserServices userServices;

    @GetMapping("/shops/all")
    public ResponseEntity<Page<ShopResponseDto>> getAllShops(Pageable pageable) {
        return successResponse(userServices.getAllShops(pageable));
    }

    @GetMapping("/profile")
    public ResponseEntity<UserProfileDTO> getUserProfile() {
        UserProfileDTO profile = userServices.getCurrentUserProfile();
        return successResponse(profile);
    }

    @PutMapping("/profile")
    public ResponseEntity<UserProfileDTO> updateUserProfile(@RequestBody UserProfileUpdateDTO updateDTO) {
        UserProfileDTO updatedProfile = userServices.updateUserProfile(updateDTO);
        return updatedResponse(updatedProfile);
    }

    @DeleteMapping("/profile")
    public ResponseEntity<Void> deleteUserAccount() {
        userServices.deleteUserAccount();
        return deletedResponse();
    }

    @GetMapping("/offers")
    public ResponseEntity<Page<OfferResponseDTO>> getUserOffers(Pageable pageable) {
        Page<OfferResponseDTO> offers = userServices.getUserOffers(pageable);
        return successResponse(offers);
    }

    @GetMapping("offers/{offerId}")
    public ResponseEntity<OfferResponseDTO> getOfferById(@PathVariable UUID offerId) {
        OfferResponseDTO offer = userServices.getOfferById(offerId);
        return successResponse(offer);
    }

    @GetMapping("{shopId}/{categoryId}")
    public ResponseEntity<Page<ProductResponseDTO>> getShopsByCategory(@PathVariable UUID categoryId,
            @PathVariable UUID shopId, Pageable pageable) {
        Page<ProductResponseDTO> shops = userServices.getShopsByCategory(categoryId, shopId, pageable);
        return successResponse(shops);
    }

}
