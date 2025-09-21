package com.techRestore.tech.restore.shop.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.techRestore.tech.restore.common.model.entities.Shop;
import com.techRestore.tech.restore.common.model.enums.Status;

import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ShopRepository extends JpaRepository<Shop, UUID> {
        Optional<Shop> findByEmail(String email);

        boolean existsByEmail(String email);

        @Query("SELECT s FROM Shop s WHERE s.name LIKE %:name%")
        Page<Shop> findByName(String name, Pageable pageable);

        @Query("SELECT s FROM Shop s WHERE s.verified = true")
        Page<Shop> findAllApprovedShops(Pageable pageable);

        @Query("SELECT s FROM Shop s WHERE s.verified = false")
        Page<Shop> findAllSuspendedShops(Pageable pageable);

        @Query("SELECT s FROM Shop s WHERE s.verified = true")
        Page<Shop> findAllVerified(Pageable pageable);

        Optional<Shop> findAllByStatus(Status status);

        @Query("""
                        SELECT COALESCE(SUM(o.totalPrice), 0)
                        FROM Order o
                        WHERE o.shopId = :shopId
                        AND o.createdAt BETWEEN :startOfDay AND :endOfDay
                        """)
        BigDecimal calculateTotalOrderSales(@Param("shopId") UUID shopId,
                        @Param("startOfDay") LocalDateTime startOfDay,
                        @Param("endOfDay") LocalDateTime endOfDay);

        @Query("""
                        SELECT COUNT(o)
                        FROM Order o
                        WHERE o.shopId = :shopId
                        AND o.createdAt BETWEEN :startOfDay AND :endOfDay
                        """)
        Long countOrdersByShopId(@Param("shopId") UUID shopId,
                        @Param("startOfDay") LocalDateTime startOfDay,
                        @Param("endOfDay") LocalDateTime endOfDay);

        @Query("""
                        SELECT COALESCE(SUM(r.price), 0)
                        FROM RepairRequest r
                        WHERE r.shopId = :shopId
                        AND r.createdAt BETWEEN :startOfDay AND :endOfDay
                        """)
        BigDecimal calculateTotalRepairSales(@Param("shopId") UUID shopId,
                        @Param("startOfDay") LocalDateTime startOfDay,
                        @Param("endOfDay") LocalDateTime endOfDay);

        @Query("""
                        SELECT COUNT(r)
                        FROM RepairRequest r
                        WHERE r.shopId = :shopId
                        AND DATE(r.createdAt) = :date
                        """)
        Long countRepairsByShopId(@Param("shopId") UUID shopId,
                        @Param("date") LocalDate date);

    @Query("SELECT s FROM Shop s LEFT JOIN FETCH s.addresses WHERE s.id = :id")
    Optional<Shop> findByIdWithAddresses(@Param("id") UUID id);
}
