package com.techRestore.tech.restore.repository;

import com.techRestore.tech.restore.model.entities.RepairPayment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RepairPaymentRepository extends JpaRepository<RepairPayment, UUID> {
    List<RepairPayment> findByUserId(UUID userId);
    Optional<RepairPayment> findByRepairRequestId(UUID repairRequestId);
    Optional<RepairPayment> findByPaymentReference(String orderIdStr);
}
