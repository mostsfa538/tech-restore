package com.techRestore.tech.restore.user.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.techRestore.tech.restore.common.model.entities.OrderItem;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, UUID> {
    Optional<OrderItem> findByIdAndOrderId(UUID id, UUID orderId);

    Optional<OrderItem> findByOrderIdAndProductId(UUID orderId, UUID productId);

    List<OrderItem> findByOrderId(UUID orderId);

    @Query("SELECT oi FROM OrderItem oi WHERE oi.orderId IN :orderIds")
    List<OrderItem> findByOrderIds(@Param("orderIds") List<UUID> orderIds);
}