package com.techRestore.tech.restore.controller.user;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.techRestore.tech.restore.dto.repair.RepairRequestCreateDto;
import com.techRestore.tech.restore.dto.repair.RepairRequestDto;
import com.techRestore.tech.restore.dto.repair.RepairStatusDto;
import com.techRestore.tech.restore.model.entities.RepairRequest;
import com.techRestore.tech.restore.services.repair.RepairRequestService;

@RestController
@RequestMapping("/api/repair-request")
public class UserRepairController {
    @Autowired
    private RepairRequestService repairRequestService;

    @GetMapping
    public ResponseEntity<List<RepairRequest>> getAllRepairRequests() {
        return ResponseEntity.ok().body(repairRequestService.getAllRepairRequest());
    }

    @PostMapping
    public ResponseEntity<RepairRequestDto> createRepairRequest(@RequestBody RepairRequestCreateDto repairRequest) {
        RepairRequestDto createdRequest = repairRequestService.createRepairRequest(repairRequest);
        return ResponseEntity.status(201).body(createdRequest);
    }

    @GetMapping("/{request_id}")
    public ResponseEntity<RepairRequestDto> getRepairRequestById(@PathVariable UUID requestId) {
        RepairRequestDto repairRequest = repairRequestService.getRepairRequestById(requestId);
        return ResponseEntity.ok().body(repairRequest);
    }

    @PutMapping("/{request_id}")
    public ResponseEntity<RepairRequestDto> updateRepairRequest(
            @PathVariable UUID requestId,
            @RequestBody RepairRequestCreateDto repairRequest) {
        RepairRequestDto updatedRequest = repairRequestService.updateRepairRequest(requestId, repairRequest);
        return ResponseEntity.ok().body(updatedRequest);
    }

    @DeleteMapping("{request_id}/cancel")
    public ResponseEntity<String> deleteRepairRequest(@PathVariable UUID requestId) {
        repairRequestService.deleteRepairRequest(requestId);
        return ResponseEntity.ok().body("Repair request deleted successfully.");
    }

    @PutMapping("/{request_id}/status")
    public ResponseEntity<String> confirmRepairRequest(@PathVariable UUID requestId, @RequestBody RepairStatusDto repairStatusDto) {
        repairRequestService.setStatus(requestId, repairStatusDto);
        return ResponseEntity.ok().body("Repair request confirmed successfully.");
    }
}
