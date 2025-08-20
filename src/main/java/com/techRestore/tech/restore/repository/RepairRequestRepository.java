package com.techRestore.tech.restore.repository;

import com.techRestore.tech.restore.model.entities.RepairRequest;
import com.techRestore.tech.restore.model.enums.RepairStatus;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface RepairRequestRepository extends JpaRepository<RepairRequest, UUID> {
    
    Page<RepairRequest> findByStatus(RepairStatus status, Pageable pageable);
    Page<RepairRequest> getAllRepairRequestByUserId(UUID userId, Pageable pageable);
    Page<RepairRequest> findAllByShopId(UUID shopId, Pageable pageable);
}
