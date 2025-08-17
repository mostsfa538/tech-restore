package com.techRestore.tech.restore.controller.shop;

import java.util.List;
import java.util.UUID;

import com.techRestore.tech.restore.dto.repair.RepairPriceUpdateDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.techRestore.tech.restore.dto.repair.RepairRequestDto;
import com.techRestore.tech.restore.dto.repair.RepairStatusDto;
import com.techRestore.tech.restore.model.enums.RepairStatus;
import com.techRestore.tech.restore.services.shop.ShopRepairService;

@RestController
@RequestMapping("/api/shops/repair-request")
public class ShopRepairController {
    @Autowired
    private ShopRepairService shopRepairService;

    @GetMapping
    public ResponseEntity<List<RepairRequestDto>> getAllRepairRequests() {
        return ResponseEntity.ok().body(shopRepairService.getAllRepairRequest());
    }

    @GetMapping("/{requestId}")
    public ResponseEntity<RepairRequestDto> getRepairRequestById(@PathVariable UUID requestId) {
        RepairRequestDto repairRequest = shopRepairService.getRepairRequestById(requestId);
        return ResponseEntity.ok().body(repairRequest);
    }

    @PutMapping("/{requestId}/status")
    public ResponseEntity<String> updateStatus(@PathVariable UUID requestId, @RequestBody RepairStatusDto statusDto) {
        shopRepairService.setStatus(requestId, statusDto);
        return ResponseEntity.ok().body("Updated Success");
    }    

    @GetMapping("/status/{status}")
    public ResponseEntity<List<RepairRequestDto>> getRepairsByStatus(
            @PathVariable RepairStatus status) {
        List<RepairRequestDto> repairs = shopRepairService.getRepairsByStatus(status);
        return ResponseEntity.ok(repairs);
    }

    @PutMapping("/{requestId}/price")
    public ResponseEntity<String> setPrice(@PathVariable UUID requestId, @RequestBody RepairPriceUpdateDto repairPriceUpdateDto) {
        shopRepairService.setPrice(requestId, repairPriceUpdateDto);
        return ResponseEntity.ok().body("Price updated Successfully");
    }
}
