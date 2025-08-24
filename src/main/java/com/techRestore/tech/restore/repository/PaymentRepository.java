package com.techRestore.tech.restore.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.techRestore.tech.restore.model.entities.Payment;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, UUID> {
    List<Payment> findByUserIdAndType(UUID userId, String type);
    List<Payment> findByUserId(UUID userId);
}
