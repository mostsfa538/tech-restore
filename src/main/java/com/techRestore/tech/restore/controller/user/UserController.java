package com.techRestore.tech.restore.controller.user;

import com.techRestore.tech.restore.controller.BaseController;
import com.techRestore.tech.restore.dto.order.OrderResponseDTO;
import com.techRestore.tech.restore.dto.product.ProductResponseDTO;
import com.techRestore.tech.restore.dto.repair.RepairRequestDto;
import com.techRestore.tech.restore.dto.user.UserProfileDTO;
import com.techRestore.tech.restore.dto.user.UserProfileUpdateDTO;
import com.techRestore.tech.restore.services.user.UserServices;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController extends BaseController {

    private final UserServices userServices;

    @GetMapping("/{shopId}/{categoryId}")
    public ResponseEntity<Page<ProductResponseDTO>> getProductsOfShopWithCategory(
            @PathVariable UUID shopId,
            @PathVariable UUID categoryId,
            Pageable pageable) {
        Page<ProductResponseDTO> products = userServices.getProductsByCategory(shopId, categoryId, pageable);
        return successResponse(products);
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

    @GetMapping("/repair-requests")
    public ResponseEntity<Page<RepairRequestDto>> getUserRepairRequests(Pageable pageable) {
        Page<RepairRequestDto> repairRequests = userServices.getUserRepairRequests(pageable);
        return successResponse(repairRequests);
    }

    @GetMapping("/orders")
    public ResponseEntity<Page<OrderResponseDTO>> getUserOrders(Pageable pageable) {
        Page<OrderResponseDTO> orders = userServices.getUserOrders(pageable);
        return ResponseEntity.ok(orders);
    }

}
