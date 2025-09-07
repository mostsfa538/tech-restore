package com.techRestore.tech.restore.shop.controller;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.techRestore.tech.restore.common.controller.BaseController;
import com.techRestore.tech.restore.common.model.enums.RepairStatus;
import com.techRestore.tech.restore.shop.service.ShopRepairService;
import com.techRestore.tech.restore.user.dto.repair.RepairPriceUpdateDto;
import com.techRestore.tech.restore.user.dto.repair.RepairRequestDto;
import com.techRestore.tech.restore.user.dto.repair.RepairStatusDto;

@RestController
@RequestMapping("/api/shops/repair-request")
public class ShopRepairController extends BaseController {
    @Autowired
    private ShopRepairService shopRepairService;

    @GetMapping
    public ResponseEntity<Page<RepairRequestDto>> getAllRepairRequests(Pageable pageable) {
        return successResponse(shopRepairService.getAllRepairRequest(pageable));
    }

    @GetMapping("/{requestId}")
    public ResponseEntity<RepairRequestDto> getRepairRequestById(@PathVariable UUID requestId) {
        RepairRequestDto repairRequest = shopRepairService.getRepairRequestById(requestId);
        return successResponse(repairRequest);
    }

    @PutMapping("/{requestId}/status")
    public ResponseEntity<String> updateStatus(@PathVariable UUID requestId, @RequestBody RepairStatusDto statusDto) {
        shopRepairService.setStatus(requestId, statusDto);
        return updatedResponse("Updated Success");
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<Page<RepairRequestDto>> getRepairsByStatus(
            @PathVariable RepairStatus status,
            Pageable pageable) {
        Page<RepairRequestDto> repairs = shopRepairService.getRepairsByStatus(status, pageable);
        return successResponse(repairs);
    }

    @PutMapping("/{requestId}/price")
    public ResponseEntity<String> setPrice(@PathVariable UUID requestId,
            @RequestBody RepairPriceUpdateDto repairPriceUpdateDto) {
        shopRepairService.setPrice(requestId, repairPriceUpdateDto);
        return updatedResponse("Price updated Successfully");
    }
}
