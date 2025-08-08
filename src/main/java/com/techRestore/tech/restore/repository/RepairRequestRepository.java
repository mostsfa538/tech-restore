package com.techRestore.tech.restore.repository;

import com.techRestore.tech.restore.model.entities.RepairRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface RepairRequestRepository extends JpaRepository<RepairRequest, UUID> {
}
