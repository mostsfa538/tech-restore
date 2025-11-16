package com.techRestore.tech.restore.common.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.techRestore.tech.restore.common.model.entities.Payment;
import com.techRestore.tech.restore.common.model.entities.User;
import com.techRestore.tech.restore.common.model.enums.PaymentMethod;
import com.techRestore.tech.restore.common.model.enums.PaymentStatus;
import com.techRestore.tech.restore.common.model.enums.PaymentType;

import jakarta.persistence.LockModeType;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, UUID> {
    List<Payment> findByUserAndPaymentType(User user, PaymentType paymentType);

    List<Payment> findByUserId(UUID userId);

    Optional<Payment> findByOrderId(UUID orderId);

    Optional<Payment> findByTransactionId(String transactionId);

    boolean existsByOrderIdAndPaymentStatus(UUID orderId, PaymentStatus status);
    
    boolean existsByRepairRequestIdAndPaymentStatus(UUID repairRequestId, PaymentStatus status);
    
    boolean existsBySubscriptionIdAndPaymentStatus(UUID subscriptionId, PaymentStatus status);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Payment p WHERE p.transactionId = :transactionId")
    Optional<Payment> findByTransactionIdForUpdate(@Param("transactionId") String transactionId);

    Optional<Payment> findByRepairRequestId(UUID repairRequestId);

    List<Payment> findByShopIdAndPaymentStatus(UUID shopId, PaymentStatus paymentStatus);

    List<Payment> findByShopIdAndPaymentTypeAndPaymentStatus(UUID shopId, PaymentType paymentType, PaymentStatus paymentStatus);
    
    Page<Payment> findAllByUserId(UUID userId, Pageable pageable);

    Page<Payment> findAllByPaymentMethodAndPaymentStatusAndPaymentType(
            PaymentMethod method,
            PaymentStatus status,
            PaymentType type,
            Pageable pageable
    );
}
