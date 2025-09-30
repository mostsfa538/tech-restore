package com.techRestore.tech.restore.common.repository;

import com.techRestore.tech.restore.common.model.entities.AssignmentLog;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AssignmentLogRepository extends JpaRepository<AssignmentLog, UUID> {
    Page<AssignmentLog> findByAssignerId(UUID assignerId, Pageable pageable);

    long countByAssignerId(UUID assignerId);
}
