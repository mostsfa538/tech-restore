package com.techRestore.tech.restore.user.repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.techRestore.tech.restore.common.model.entities.Review;

@Repository
public interface ReviewRepository extends JpaRepository<Review, UUID> {
  Page<Review> findAllByShopId(UUID shopId, Pageable pageable);

  Page<Review> findAllByUserId(UUID userId, Pageable pageable);

  boolean existsByUserIdAndShopId(UUID userId, UUID shopId);

  long countByShopId(UUID shopId);

  List<Review>findAllByShopId(UUID shopId);

}
