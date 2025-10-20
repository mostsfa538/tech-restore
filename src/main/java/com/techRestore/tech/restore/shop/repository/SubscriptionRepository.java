package com.techRestore.tech.restore.shop.repository;

import com.techRestore.tech.restore.common.model.entities.Subscription;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;
import java.util.List;
import java.util.Optional;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, UUID> {
    Optional<Subscription> findByPaymentId(UUID paymentId);
    Optional<Subscription> findFirstByShopIdOrderByCreatedAtDesc(UUID shopId);
    List<Subscription> findAllByShopIdOrderByCreatedAtDesc(UUID shopId);
}