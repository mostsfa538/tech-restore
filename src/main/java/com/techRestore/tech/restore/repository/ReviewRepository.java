package com.techRestore.tech.restore.repository;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.techRestore.tech.restore.model.entities.Review;

@Repository
public interface ReviewRepository extends JpaRepository<Review, UUID> {
  Page<Review> findAllByShopId(UUID shopId, Pageable pageable);

  Page<Review> findAllByUserId(UUID userId, Pageable pageable);

  boolean existsByUserIdAndShopId(UUID userId, UUID shopId);
}
