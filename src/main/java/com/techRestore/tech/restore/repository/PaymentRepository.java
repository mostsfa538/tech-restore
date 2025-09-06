package com.techRestore.tech.restore.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.techRestore.tech.restore.model.entities.Payment;
import com.techRestore.tech.restore.model.entities.User;
import com.techRestore.tech.restore.model.enums.PaymentType;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, UUID> {
    List<Payment> findByUserAndPaymentType(User user, PaymentType paymentType);

    List<Payment> findByUserId(UUID userId);

    Optional<Payment> findByOrderId(UUID orderId);

    Optional<Payment> findByTransactionId(String transactionId);
}
