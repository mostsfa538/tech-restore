package com.techRestore.tech.restore.user.controller;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
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
import com.techRestore.tech.restore.user.dto.repair.RepairRequestCreateDto;
import com.techRestore.tech.restore.user.dto.repair.RepairRequestDto;
import com.techRestore.tech.restore.user.dto.repair.RepairRequestUpdateDto;
import com.techRestore.tech.restore.user.dto.repair.RepairStatusDto;
import com.techRestore.tech.restore.user.service.user.RepairRequestService;

@RestController
@RequestMapping("/api/users/repair-request")
public class UserRepairController extends BaseController {
    @Autowired
    private RepairRequestService repairRequestService;

    @GetMapping
    public ResponseEntity<Page<RepairRequestDto>> getAllRepairRequests(Pageable pageable) {
        return successResponse(repairRequestService.getAllRepairRequestByUserId(pageable));
    }

    @PostMapping("/{shopId}")
    public ResponseEntity<RepairRequestDto> createRepairRequest(
            @PathVariable UUID shopId,
            @RequestBody RepairRequestCreateDto repairRequest) {
        RepairRequestDto createdRequest = repairRequestService.createRepairRequest(shopId, repairRequest);
        return createdResponse(createdRequest);
    }

    @GetMapping("/{requestId}")
    public ResponseEntity<RepairRequestDto> getRepairRequestById(@PathVariable UUID requestId) {
        RepairRequestDto repairRequest = repairRequestService.getRepairRequestById(requestId);
        return successResponse(repairRequest);
    }

    @PutMapping("/{shopId}/{requestId}")
    public ResponseEntity<RepairRequestDto> updateRepairRequest(
            @PathVariable UUID shopId,
            @PathVariable UUID requestId,
            @RequestBody RepairRequestUpdateDto repairRequest) {
        RepairRequestDto updatedRequest = repairRequestService.updateRepairRequest(shopId, requestId, repairRequest);
        return updatedResponse(updatedRequest);
    }

    @DeleteMapping("{requestId}/cancel")
    public ResponseEntity<Void> deleteRepairRequest(@PathVariable UUID requestId) {
        repairRequestService.deleteRepairRequest(requestId);
        return deletedResponse();
    }

    @PutMapping("/{requestId}/status")
    public ResponseEntity<Void> confirmRepairRequest(@PathVariable UUID requestId,
            @RequestBody RepairStatusDto repairStatusDto) {
        repairRequestService.setStatus(requestId, repairStatusDto);
        return updatedResponse();
    }
}
