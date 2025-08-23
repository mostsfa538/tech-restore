package com.techRestore.tech.restore.repository;

import com.techRestore.tech.restore.model.entities.CartItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, UUID> {
    Optional<CartItem> findByUserIdAndProductId(UUID userId, UUID productId);

    Optional<CartItem> findByIdAndUserId(UUID itemId, UUID userId);

    List<CartItem> findByUserId(UUID userId);
    Page<CartItem>findByUserId(UUID userId, Pageable pageable);

    void deleteAllByUserId(UUID userId);
}
