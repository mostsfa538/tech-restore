package com.techRestore.tech.restore.controller.admin;

import com.techRestore.tech.restore.dto.common.SearchRequest;
import com.techRestore.tech.restore.dto.shop.ShopResponseDto;
import com.techRestore.tech.restore.dto.user.ResponseUsersDto;
import com.techRestore.tech.restore.dto.user.UpdateRoleRequest;
import com.techRestore.tech.restore.model.entities.RepairRequest;
import com.techRestore.tech.restore.model.entities.Shop;
import com.techRestore.tech.restore.services.admin.AdminServices;
import com.techRestore.tech.restore.services.repair.RepairRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin")
public class AdminController {
    @Autowired
    private RepairRequestService repairRequestService;

    @Autowired
    private AdminServices adminServices;

    @GetMapping("/repair-requests")
    public ResponseEntity<List<RepairRequest>> getAllRepairRequests(){
        List<RepairRequest> repairRequests = repairRequestService.getAllRepairRequest();
        return ResponseEntity.ok().body(repairRequests);
    }

    @GetMapping("/users")
    public ResponseEntity<List<ResponseUsersDto>> getAllUsers() {
        return ResponseEntity.ok().body(adminServices.getAllUsers());
    }

    @GetMapping("/users/{userId}")
    public ResponseEntity<ResponseUsersDto> getUserById(@PathVariable UUID userId) {
        return ResponseEntity.ok().body(adminServices.getUserDetailsById(userId));
    }

    @PutMapping("/users/{userId}")
    public ResponseEntity<Void> updateRole(@PathVariable UUID userId, @RequestBody UpdateRoleRequest role) {
        adminServices.updateRole(userId, role.role());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/shops")
    public ResponseEntity<List<ShopResponseDto>> getAllShops() {
        return ResponseEntity.ok().body(adminServices.getShops());
    }

    @GetMapping("/shops/{shopId}")
    public ResponseEntity<ShopResponseDto> getShopById(@PathVariable UUID shopId) {
        return ResponseEntity.ok().body(adminServices.getShopById(shopId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteShopById(@PathVariable UUID id) {
        adminServices.deleteShop(id);
        return ResponseEntity.ok().body("Removed Success");
    }

    @GetMapping("/search")
    public ResponseEntity<?> searchByName(@RequestBody SearchRequest searchRequest) {
        List<Shop> shop = adminServices.search(searchRequest.name());
        return  ResponseEntity.ok().body(shop);
    }


    @GetMapping("/shops/approved")
    public ResponseEntity<List<ShopResponseDto>> getAllApprovedShops() {
        return ResponseEntity.ok().body(adminServices.getApprovedShops());
    }

    @GetMapping("/shops/suspend")
    public ResponseEntity<List<ShopResponseDto>> getAllSuspendedShops() {
        return ResponseEntity.ok().body(adminServices.getSuspendedShops());
    }

    @PutMapping("/shops/{shopId}/approve")
    public ResponseEntity<Void> approveShop(@PathVariable UUID shopId) {
        adminServices.approveShop(shopId);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/shops/{shopId}/suspend")
    public ResponseEntity<Void> suspendShop(@PathVariable UUID shopId) {
        adminServices.suspendShop(shopId);
        return ResponseEntity.ok().build();
    }
}
