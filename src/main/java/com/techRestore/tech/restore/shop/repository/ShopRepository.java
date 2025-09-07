package com.techRestore.tech.restore.shop.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.techRestore.tech.restore.common.model.entities.Shop;

import org.springframework.data.domain.Pageable;

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
}
