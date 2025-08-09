package com.techRestore.tech.restore.repository;

import com.techRestore.tech.restore.model.entities.RepairRequest;
import com.techRestore.tech.restore.model.enums.RepairStatus;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface RepairRequestRepository extends JpaRepository<RepairRequest, UUID> {
    
    public List<RepairRequest> findByStatus(RepairStatus status);
}
