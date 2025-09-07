package com.techRestore.tech.restore.admin.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.techRestore.tech.restore.common.model.entities.Review;

@Repository
public interface AdminReviewRepository extends JpaRepository<Review, UUID> {

    Optional<Review> findById(UUID id);

    void delete(Review review);

}
