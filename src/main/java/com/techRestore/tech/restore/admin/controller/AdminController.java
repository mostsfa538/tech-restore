package com.techRestore.tech.restore.admin.controller;

import com.techRestore.tech.restore.admin.service.AdminServices;
import com.techRestore.tech.restore.common.controller.BaseController;
import com.techRestore.tech.restore.common.dto.common.SearchRequest;
import com.techRestore.tech.restore.common.model.entities.Shop;
import com.techRestore.tech.restore.shop.dto.shop.ShopResponseDto;
import com.techRestore.tech.restore.user.dto.repair.RepairRequestDto;
import com.techRestore.tech.restore.user.dto.user.ResponseUsersDto;
import com.techRestore.tech.restore.user.dto.user.UpdateRoleRequest;
import com.techRestore.tech.restore.user.service.user.RepairRequestService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/admin")
public class AdminController extends BaseController {
	@Autowired
	private RepairRequestService repairRequestService;

	@Autowired
	private AdminServices adminServices;

	@GetMapping("/repair-requests")
	public ResponseEntity<Page<RepairRequestDto>> getAllRepairRequests(Pageable pageable) {
		Page<RepairRequestDto> repairRequests = repairRequestService.getAllRepairRequest(pageable);
		return successResponse(repairRequests);
	}

	@GetMapping("/users")
	public ResponseEntity<Page<ResponseUsersDto>> getAllUsers(Pageable pageable) {
		return successResponse(adminServices.getAllUsers(pageable));
	}

	@GetMapping("/users/{userId}")
	public ResponseEntity<ResponseUsersDto> getUserById(@PathVariable UUID userId) {
		return successResponse(adminServices.getUserDetailsById(userId));
	}

	@PutMapping("/users/{userId}")
	public ResponseEntity<Void> updateRole(@PathVariable UUID userId, @RequestBody UpdateRoleRequest role) {
		adminServices.updateRole(userId, role.role());
		return updatedResponse();
	}

	@PutMapping("/users/{userId}/deactivate")
	public ResponseEntity<Void> suspendUser(@PathVariable UUID userId) {
		adminServices.suspendUser(userId);
		return updatedResponse();
	}

	@PutMapping("/users/{userId}/activate")
	public ResponseEntity<Void> approveUser(@PathVariable UUID userId) {
		adminServices.approveUser(userId);
		return updatedResponse();
	}

	@GetMapping("/shops")
	public ResponseEntity<Page<ShopResponseDto>> getAllShops(Pageable pageable) {
		return ResponseEntity.ok().body(adminServices.getShops(pageable));
	}

	@GetMapping("/shops/{shopId}")
	public ResponseEntity<ShopResponseDto> getShopById(@PathVariable UUID shopId) {
		return successResponse(adminServices.getShopById(shopId));
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> deleteShopById(@PathVariable UUID id) {
		adminServices.deleteShop(id);
		return deletedResponse();
	}

	@GetMapping("/search")
	public ResponseEntity<?> searchByName(@RequestBody SearchRequest searchRequest, Pageable pageable) {
		Page<Shop> shop = adminServices.search(searchRequest.name(), pageable);
		return successResponse(shop);
	}

	@GetMapping("/shops/approved")
	public ResponseEntity<Page<ShopResponseDto>> getAllApprovedShops(Pageable pageable) {
		return successResponse(adminServices.getApprovedShops(pageable));
	}

	@GetMapping("/shops/suspend")
	public ResponseEntity<Page<ShopResponseDto>> getAllSuspendedShops(Pageable pageable) {
		return successResponse(adminServices.getSuspendedShops(pageable));
	}

	@PutMapping("/shops/{shopId}/approve")
	public ResponseEntity<Void> approveShop(@PathVariable UUID shopId) {
		adminServices.approveShop(shopId);
		return updatedResponse();
	}

	@PutMapping("/shops/{shopId}/suspend")
	public ResponseEntity<Void> suspendShop(@PathVariable UUID shopId) {
		adminServices.suspendShop(shopId);
		return updatedResponse();
	}
}
