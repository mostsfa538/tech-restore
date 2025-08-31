package com.techRestore.tech.restore.controller.delivery;

import com.techRestore.tech.restore.controller.BaseController;
import com.techRestore.tech.restore.dto.delivery.DeliveryProfileUpdateDto;
import com.techRestore.tech.restore.dto.delivery.DeliveryStateUpdate;
import com.techRestore.tech.restore.dto.delivery.OrderDeliveryDto;
import com.techRestore.tech.restore.model.entities.Delivery;
import com.techRestore.tech.restore.services.delivery.DeliveryService;

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

    @GetMapping("/profile")
    @PreAuthorize("hasRole('DELIVERY')")
    public ResponseEntity<Delivery> getProfile() {
        return successResponse(deliveryService.getProfile());
    }

    @PutMapping("/profile")
    @PreAuthorize("hasRole('DELIVERY')")
    public ResponseEntity<String> updateProfile(@RequestBody DeliveryProfileUpdateDto updateDto) {
        deliveryService.updateProfile(updateDto);
        return updatedResponse("Profile updated successfully");
    }

    @GetMapping("/orders/available")
    @PreAuthorize("hasRole('DELIVERY')")
    public ResponseEntity<Page<OrderDeliveryDto>> getAvailableOrders(Pageable pageable) {
        return successResponse(deliveryService.getAvailableOrders(pageable));
    }

    @GetMapping("/orders/my-deliveries")
    @PreAuthorize("hasRole('DELIVERY')")
    public ResponseEntity<Page<OrderDeliveryDto>> getMyDeliveries(Pageable pageable) {
        return successResponse(deliveryService.getMyDeliveries(pageable));
    }

    @PostMapping("/orders/{orderId}/accept")
    @PreAuthorize("hasRole('DELIVERY')")
    public ResponseEntity<String> acceptDelivery(@PathVariable UUID orderId) {
        deliveryService.acceptDelivery(orderId);
        return createdResponse("Delivery accepted successfully");
    }

    @PostMapping("/orders/{orderId}/reject")
    @PreAuthorize("hasRole('DELIVERY')")
    public ResponseEntity<String> rejectDelivery(@PathVariable UUID orderId) {
        deliveryService.rejectDelivery(orderId);
        return successResponse("Delivery rejected successfully");
    }

    @PutMapping("/orders/{orderId}/status")
    @PreAuthorize("hasRole('DELIVERY')")
    public ResponseEntity<String> updateOrderStatus(
            @PathVariable UUID orderId, 
            @RequestBody DeliveryStateUpdate statusUpdate) {
        deliveryService.updateOrderStatus(orderId, statusUpdate);
        return updatedResponse("Order status updated successfully");
    }
}