package com.techRestore.tech.restore.common.repository;

import com.techRestore.tech.restore.assigners.dto.AssignmentLogDto;
import com.techRestore.tech.restore.common.model.entities.AssignmentLog;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.UUID;

public interface AssignmentLogRepository extends JpaRepository<AssignmentLog, UUID> {
    Page<AssignmentLog> findByAssignerId(UUID assignerId, Pageable pageable);
    long countByAssignerId(UUID assignerId);
}

