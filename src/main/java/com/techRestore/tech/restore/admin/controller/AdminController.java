package com.techRestore.tech.restore.admin.controller;

import com.techRestore.tech.restore.admin.dto.AdminStatsDto;
import com.techRestore.tech.restore.admin.service.AdminServices;
import com.techRestore.tech.restore.assigners.dto.AssignerResponseDto;
import com.techRestore.tech.restore.assigners.dto.AssignmentLogDto;
import com.techRestore.tech.restore.assigners.service.AssignerService;
import com.techRestore.tech.restore.common.controller.BaseController;
import com.techRestore.tech.restore.common.dto.common.SearchRequest;
import com.techRestore.tech.restore.common.dto.payment.AdminPaymentDto;
import com.techRestore.tech.restore.common.dto.payment.PaymentDto;
import com.techRestore.tech.restore.common.model.entities.Shop;
import com.techRestore.tech.restore.common.services.payment.PaymentService;
import com.techRestore.tech.restore.delivery.dto.DeliveryResponseDto;
import com.techRestore.tech.restore.shop.dto.offers.OfferResponseDTO;
import com.techRestore.tech.restore.shop.dto.shop.ShopResponseDto;
import com.techRestore.tech.restore.user.dto.repair.RepairRequestDto;
import com.techRestore.tech.restore.user.dto.user.ResponseUsersDto;
import com.techRestore.tech.restore.user.dto.user.UpdateRoleRequest;
import com.techRestore.tech.restore.user.service.RepairRequestService;
import com.techRestore.tech.restore.user.service.UserOrderService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PathVariable;

@RestController
@RequestMapping("/api/admin")
public class AdminController extends BaseController {
	@Autowired
	private RepairRequestService repairRequestService;

	@Autowired
	private PaymentService paymentService;

	@Autowired
	private AdminServices adminServices;

	@Autowired
	private AssignerService assignerService;

	@Autowired
	private UserOrderService orderService;

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

	@GetMapping("/offers")
	public ResponseEntity<Page<OfferResponseDTO>> getAllShopsWithOffers(Pageable pageable) {
		return successResponse(adminServices.getAllOffers(pageable));
	}

	@DeleteMapping("/offers/{offerId}")
	public ResponseEntity<Void> deleteOffer(@PathVariable UUID offerId) {
		adminServices.deleteOffer(offerId);
		return deletedResponse();
	}

	@GetMapping("/transactions/all")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<Page<AdminPaymentDto>> getAllTransactions(Pageable pageable) {
		Page<AdminPaymentDto> transactions = paymentService.getAllTransactions(pageable);
		return successResponse(transactions);
	}

	@GetMapping("/transactions/{userId}")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<Page<PaymentDto>> getTransactionByUserId(@PathVariable UUID userId, Pageable pageable) {
		Page<PaymentDto> transactions = paymentService.getAllUserTransactions(userId, pageable);
		return successResponse(transactions);
	}

	@GetMapping("/assignment-logs")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<Page<AssignmentLogDto>> getAllAssignmentLogs(Pageable pageable) {
		return successResponse(assignerService.getAllAssignmentLogs(pageable));
	}

	@GetMapping("/deliveries")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<Page<DeliveryResponseDto>> getAllDeliveries(Pageable pageable) {
		return successResponse(adminServices.getAllDeliveries(pageable));
	}

	@GetMapping("/deliveries/{deliveryId}")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<DeliveryResponseDto> getDeliveryById(@PathVariable UUID deliveryId) {
		return successResponse(adminServices.getDeliveryById(deliveryId));
	}

	@GetMapping("/deliveries/pending")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<Page<DeliveryResponseDto>> getPendingDeliveries(Pageable pageable) {
		return successResponse(adminServices.getPendingDeliveries(pageable));
	}

	@GetMapping("/deliveries/approved")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<Page<DeliveryResponseDto>> getApprovedDeliveries(Pageable pageable) {
		return successResponse(adminServices.getApprovedDeliveries(pageable));
	}

	@GetMapping("/deliveries/suspended")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<Page<DeliveryResponseDto>> getSuspendedDeliveries(Pageable pageable) {
		return successResponse(adminServices.getSuspendedDeliveries(pageable));
	}

	@PutMapping("/deliveries/{deliveryId}/approve")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<Void> approveDelivery(@PathVariable UUID deliveryId) {
		adminServices.approveDelivery(deliveryId);
		return updatedResponse();
	}

	@PutMapping("/deliveries/{deliveryId}/suspend")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<Void> suspendDelivery(@PathVariable UUID deliveryId) {
		adminServices.suspendDelivery(deliveryId);
		return updatedResponse();
	}

	@DeleteMapping("/deliveries/{deliveryId}")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<Void> deleteDelivery(@PathVariable UUID deliveryId) {
		adminServices.deleteDelivery(deliveryId);
		return deletedResponse();
	}

	@GetMapping("/stats")
	public ResponseEntity<AdminStatsDto> getAdminStats() {
		return ResponseEntity.ok(adminServices.getAdminStats());
	}

	@GetMapping("/assigners")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<Page<AssignerResponseDto>> getAllAssigners(Pageable pageable) {
		return successResponse(adminServices.getAllAssigners(pageable));
	}

	@GetMapping("/assigners/{assignerId}")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<AssignerResponseDto> getAssignerById(@PathVariable UUID assignerId) {
		return successResponse(adminServices.getAssignerById(assignerId));
	}

	@GetMapping("/assigners/pending")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<Page<AssignerResponseDto>> getPendingAssigners(Pageable pageable) {
		return successResponse(adminServices.getPendingAssigners(pageable));
	}

	@GetMapping("/assigners/approved")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<Page<AssignerResponseDto>> getApprovedAssigners(Pageable pageable) {
		return successResponse(adminServices.getApprovedAssigners(pageable));
	}

	@GetMapping("/assigners/suspended")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<Page<AssignerResponseDto>> getSuspendedAssigners(Pageable pageable) {
		return successResponse(adminServices.getSuspendedAssigners(pageable));
	}

	@PutMapping("/assigners/{assignerId}/approve")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<Void> approveAssigner(@PathVariable UUID assignerId) {
		adminServices.approveAssigner(assignerId);
		return updatedResponse();
	}

	@PutMapping("/assigners/{assignerId}/suspend")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<Void> suspendAssigner(@PathVariable UUID assignerId) {
		adminServices.suspendAssigner(assignerId);
		return updatedResponse();
	}

	@DeleteMapping("/assigners/{assignerId}")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<Void> deleteAssigner(@PathVariable UUID assignerId) {
		adminServices.deleteAssigner(assignerId);
		return deletedResponse();
	}

	@PutMapping("/payment-refund/{orderId}")
	public ResponseEntity<Void> updateRefundStatus(@PathVariable UUID orderId) {
		orderService.updateRefundStatus(orderId);
		return updatedResponse();
	}
}
