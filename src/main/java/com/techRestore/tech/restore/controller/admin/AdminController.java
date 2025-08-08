package com.techRestore.tech.restore.controller.admin;

import com.techRestore.tech.restore.model.entities.RepairRequest;
import com.techRestore.tech.restore.services.repair.RepairRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController("/api")
public class AdminController {
    @Autowired
    private RepairRequestService repairRequestService;

    @GetMapping("/repair-requests")
    public ResponseEntity<List<RepairRequest>> getAllRepairRequests(){
        List<RepairRequest> repairRequests = repairRequestService.getAllRepairRequest();
        return ResponseEntity.ok().body(repairRequests);
    }
}
