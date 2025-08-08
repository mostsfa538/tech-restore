package com.techRestore.tech.restore.services.repair;

import com.techRestore.tech.restore.dto.repair.RepairRequestDto;
import com.techRestore.tech.restore.model.entities.RepairRequest;
import com.techRestore.tech.restore.repository.RepairRequestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RepairRequestService {
    @Autowired
    private RepairRequestRepository repairRequestRepository;

    @PreAuthorize("hasRole('ADMIN')")
    public List<RepairRequest> getAllRepairRequest() {
        return repairRequestRepository.findAll();
    }

//    public RepairRequest createRepairRequest(RepairRequestDto repairRequestDto) {
//
//    }

}
