package com.techRestore.tech.restore.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.techRestore.tech.restore.model.entities.OrderPayment;

@Repository
public interface OrderPaymentRepository extends JpaRepository<OrderPayment, UUID> {
  Optional<OrderPayment> findByOrderId(UUID orderId);

  Optional<OrderPayment> findByPaymentReference(String orderIdStr);
}
