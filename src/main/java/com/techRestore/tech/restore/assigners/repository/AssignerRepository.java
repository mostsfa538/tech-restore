package com.techRestore.tech.restore.assigners.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.techRestore.tech.restore.common.model.entities.Assigner;
import com.techRestore.tech.restore.common.model.enums.ApprovalStatus;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface AssignerRepository extends JpaRepository<Assigner, UUID> {
    Optional<Assigner> findByEmail(String email);

    boolean existsByEmail(String email);

    Page<Assigner> findByStatus(ApprovalStatus status, Pageable pageable);
}
