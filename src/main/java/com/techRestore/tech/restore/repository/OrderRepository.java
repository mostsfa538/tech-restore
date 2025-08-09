package com.techRestore.tech.restore.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.techRestore.tech.restore.model.entities.Order;
import com.techRestore.tech.restore.model.enums.OrderStatus;

@Repository
public interface OrderRepository extends JpaRepository<Order, UUID> {
  Optional<Order> findByUserIdAndStatus(UUID userId, OrderStatus status);
}
