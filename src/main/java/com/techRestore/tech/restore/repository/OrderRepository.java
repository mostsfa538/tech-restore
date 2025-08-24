package com.techRestore.tech.restore.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.techRestore.tech.restore.model.entities.Order;
import com.techRestore.tech.restore.model.enums.OrderStatus;

@Repository
public interface OrderRepository extends JpaRepository<Order, UUID> {
  Optional<Order> findByUserIdAndStatus(UUID userId, OrderStatus status);

  @Query("SELECT o FROM Order o LEFT JOIN FETCH o.orderItems WHERE o.id = :id")
  Optional<Order> findByIdWithItems(@Param("id") UUID id);

  List<Order> findByUserId(UUID userId);

  Optional<Order> findByIdAndUserId(UUID id, UUID userId);

  Optional<Order> findByPaymentId(UUID paymentId);

  Page<Order> findByUserId(UUID userId, Pageable pageable);

}
