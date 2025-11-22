package com.techRestore.tech.restore.delivery.controller;

import com.techRestore.tech.restore.common.controller.BaseController;
import com.techRestore.tech.restore.delivery.dto.DeliveryProfileUpdateDto;
import com.techRestore.tech.restore.delivery.dto.DeliveryResponseDto;
import com.techRestore.tech.restore.delivery.dto.DeliveryStateUpdate;
import com.techRestore.tech.restore.delivery.dto.OrderDeliveryDto;
import com.techRestore.tech.restore.delivery.dto.RepairDeliveryDto;
import com.techRestore.tech.restore.delivery.dto.RepairDeliveryStateUpdate;
import com.techRestore.tech.restore.delivery.service.DeliveryService;
import com.techRestore.tech.restore.delivery.service.RepairDeliveryService;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/delivery")
@RequiredArgsConstructor
@Validated
public class DeliveryController extends BaseController {

    private final DeliveryService deliveryService;
    private final RepairDeliveryService repairDeliveryService;

    @GetMapping("/profile")
    public ResponseEntity<DeliveryResponseDto> getProfile() {
        return successResponse(deliveryService.getProfile());
    }

    @PutMapping("/profile")
    public ResponseEntity<String> updateProfile(@RequestBody DeliveryProfileUpdateDto updateDto) {
        deliveryService.updateProfile(updateDto);
        return updatedResponse("Profile updated successfully");
    }

    @GetMapping("/orders/available")
    public ResponseEntity<Page<OrderDeliveryDto>> getAvailableOrders(Pageable pageable) {
        return successResponse(deliveryService.getAvailableOrders(pageable));
    }

    @GetMapping("/orders/my-deliveries")
    public ResponseEntity<Page<OrderDeliveryDto>> getMyDeliveries(Pageable pageable) {
        return successResponse(deliveryService.getMyDeliveries(pageable));
    }

    @PostMapping("/orders/{orderId}/accept")
    public ResponseEntity<String> acceptDelivery(@PathVariable UUID orderId) {
        deliveryService.acceptDelivery(orderId);
        return createdResponse("Delivery accepted successfully");
    }

    @PostMapping("/orders/{orderId}/reject")
    public ResponseEntity<String> rejectDelivery(@PathVariable UUID orderId) {
        deliveryService.rejectDelivery(orderId);
        return successResponse("Delivery rejected successfully");
    }

    @PutMapping("/orders/{orderId}/status")
    public ResponseEntity<String> updateOrderStatus(
            @PathVariable UUID orderId,
            @RequestBody DeliveryStateUpdate statusUpdate) {
        deliveryService.updateOrderStatus(orderId, statusUpdate);
        return updatedResponse("Order status updated successfully");
    }

    @GetMapping("/repair/available")
    @PreAuthorize("hasRole('DELIVERY')")
    public ResponseEntity<Page<RepairDeliveryDto>> getAvailableRepairRequests(Pageable pageable) {
        return successResponse(repairDeliveryService.getAvailableRepairRequests(pageable));
    }

    @GetMapping("/repair/my-deliveries")
    @PreAuthorize("hasRole('DELIVERY')")
    public ResponseEntity<Page<RepairDeliveryDto>> getMyRepairDeliveries(Pageable pageable) {
        return successResponse(repairDeliveryService.getMyRepairDeliveries(pageable));
    }

    @PostMapping("/repair/{repairRequestId}/accept")
    @PreAuthorize("hasRole('DELIVERY')")
    public ResponseEntity<String> acceptRepairDelivery(
            @PathVariable UUID repairRequestId) {
        repairDeliveryService.acceptRepairDelivery(repairRequestId);
        return createdResponse("Delivery accepted successfully");
    }

    @PostMapping("/repair/{repairRequestId}/reject")
    @PreAuthorize("hasRole('DELIVERY')")
    public ResponseEntity<String> rejectRepairDelivery(
            @PathVariable UUID repairRequestId) {
        repairDeliveryService.rejectRepairDelivery(repairRequestId);
        return successResponse("Repair request collection rejected successfully");
    }

    @PutMapping("/repair/{repairRequestId}/status")
    @PreAuthorize("hasRole('DELIVERY')")
    public ResponseEntity<String> updateRepairRequestStatus(
            @PathVariable UUID repairRequestId,
            @RequestBody RepairDeliveryStateUpdate statusUpdate) {
        repairDeliveryService.updateRepairRequestStatus(repairRequestId, statusUpdate);
        return updatedResponse("Repair request status updated successfully");
    }
}