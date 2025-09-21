package com.techRestore.tech.restore.assigners.controller;

import com.techRestore.tech.restore.assigners.dto.AssignerProfileUpdateDto;
import com.techRestore.tech.restore.assigners.dto.AssignmentLogDto;
import com.techRestore.tech.restore.assigners.dto.DeliveryPersonDto;
import com.techRestore.tech.restore.assigners.dto.OrderAssignmentDto;
import com.techRestore.tech.restore.assigners.dto.ReassignmentDto;
import com.techRestore.tech.restore.assigners.dto.RepairAssignmentDto;
import com.techRestore.tech.restore.assigners.service.AssignerService;
import com.techRestore.tech.restore.common.model.entities.Assigner;
import com.techRestore.tech.restore.delivery.dto.OrderDeliveryDto;
import com.techRestore.tech.restore.delivery.dto.RepairDeliveryDto;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.UUID;

@RestController
@RequestMapping("/api/assigner")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ASSIGNER')")
public class AssignerController {

    private final AssignerService assignerService;

    @GetMapping("/profile")
    public ResponseEntity<Assigner> getProfile() {
        return ResponseEntity.ok(assignerService.getProfile());
    }

    @PutMapping("/profile")
    public ResponseEntity<String> updateProfile(@Valid @RequestBody AssignerProfileUpdateDto updateDto) {
        assignerService.updateProfile(updateDto);
        return ResponseEntity.ok("Profile updated successfully");
    }

    @GetMapping("/delivery-persons")
    public ResponseEntity<Page<DeliveryPersonDto>> getAvailableDeliveryPersons(Pageable pageable) {
        return ResponseEntity.ok(assignerService.getAvailableDeliveryPersons(pageable));
    }

    @GetMapping("/orders-for-assignment")
    public ResponseEntity<Page<OrderDeliveryDto>> getOrdersForAssignment(Pageable pageable) {
        return ResponseEntity.ok(assignerService.getOrdersForAssignment(pageable));
    }

    @GetMapping("/repairs-for-assignment")
    public ResponseEntity<Page<RepairDeliveryDto>> getRepairRequestsForAssignment(Pageable pageable) {
        return ResponseEntity.ok(assignerService.getRepairRequestsForAssignment(pageable));
    }

    @PostMapping("/assign-order")
    public ResponseEntity<String> assignOrderToDelivery(@Valid @RequestBody OrderAssignmentDto assignmentDto) {
        assignerService.assignOrderToDelivery(assignmentDto);
        return ResponseEntity.ok("Order assigned successfully");
    }

    @PostMapping("/assign-repair")
    public ResponseEntity<String> assignRepairToDelivery(@Valid @RequestBody RepairAssignmentDto assignmentDto) {
        assignerService.assignRepairToDelivery(assignmentDto);
        return ResponseEntity.ok("Repair request assigned successfully");
    }

    @GetMapping("/delivery/{deliveryId}/orders")
    public ResponseEntity<Page<OrderDeliveryDto>> getAssignedOrdersByDelivery(
            @PathVariable UUID deliveryId, Pageable pageable) {
        return ResponseEntity.ok(assignerService.getAssignedOrdersByDelivery(deliveryId, pageable));
    }

    @GetMapping("/delivery/{deliveryId}/repairs")
    public ResponseEntity<Page<RepairDeliveryDto>> getAssignedRepairsByDelivery(
            @PathVariable UUID deliveryId, Pageable pageable) {
        return ResponseEntity.ok(assignerService.getAssignedRepairsByDelivery(deliveryId, pageable));
    }

    @PutMapping("/reassign-order/{orderId}")
    public ResponseEntity<String> reassignOrder(@PathVariable UUID orderId, @RequestBody ReassignmentDto reassignmentDto) {
        assignerService.reassignOrder(orderId, reassignmentDto.getNewDeliveryId(), reassignmentDto.getNotes());
        return ResponseEntity.ok("Order reassigned successfully");
    }

    @PutMapping("/reassign-repair/{repairRequestId}")
    public ResponseEntity<String> reassignRepairRequest(@PathVariable UUID repairRequestId, @RequestBody ReassignmentDto reassignmentDto) {
        assignerService.reassignRepairRequest(repairRequestId, reassignmentDto.getNewDeliveryId(), reassignmentDto.getNotes());
        return ResponseEntity.ok("Repair request reassigned successfully");
    }

    @GetMapping("/assignment-log")
    public ResponseEntity<Page<AssignmentLogDto>> getAssignerAssignmentLogs(Pageable pageable) {
        return ResponseEntity.ok(assignerService.getAssignerAssignmentLogs(pageable));
    }
}