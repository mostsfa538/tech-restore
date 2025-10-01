package com.techRestore.tech.restore.user.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.techRestore.tech.restore.common.model.entities.Order;
import com.techRestore.tech.restore.common.model.enums.OrderStatus;

@Repository
public interface OrderRepository extends JpaRepository<Order, UUID> {
  Optional<Order> findByUserIdAndStatus(UUID userId, OrderStatus status);

  @Query("SELECT o FROM Order o LEFT JOIN FETCH o.orderItems WHERE o.id = :id")
  Optional<Order> findByIdWithItems(@Param("id") UUID id);

  List<Order> findByUserId(UUID userId);

  Optional<Order> findByIdAndUserId(UUID id, UUID userId);

  Optional<Order> findByPaymentId(UUID paymentId);

  Page<Order> findByUserId(UUID userId, Pageable pageable);

  @Query("SELECT o FROM Order o JOIN o.orderItems oi WHERE oi.shopId = :shopId")
  Page<Order> findByShopId(@Param("shopId") UUID shopId, Pageable pageable);

  @Query("SELECT o FROM Order o JOIN o.orderItems oi WHERE o.id = :orderId AND oi.shopId = :shopId")
  Optional<Order> findByIdAndShopId(@Param("orderId") UUID orderId, @Param("shopId") UUID shopId);

  @Query("SELECT o FROM Order o JOIN o.orderItems oi WHERE oi.shopId = :shopId AND o.status = :status")
  Page<Order> findByStatusAndShopId(@Param("status") OrderStatus status, @Param("shopId") UUID shopId,
      Pageable pageable);

  Page<Order> findByStatusAndDeliveryIdIsNull(OrderStatus status, Pageable pageable);

  Page<Order> findByStatus(OrderStatus status, Pageable pageable);

  @Query("SELECT o FROM Order o WHERE o.deliveryId = :deliveryId")
  Page<Order> findByDeliveryId(UUID deliveryId, Pageable pageable);

  long countByDeliveryIdAndStatusIn(UUID deliveryId, List<OrderStatus> statuses);

  @Query("SELECT r FROM RepairRequest r WHERE r.deliveryId = :deliveryId AND r.status IN :statuses")
  List<Order> findByDeliveryIdAndStatusIn(@Param("deliveryId") UUID deliveryId,
      @Param("statuses") List<OrderStatus> statuses);

}
