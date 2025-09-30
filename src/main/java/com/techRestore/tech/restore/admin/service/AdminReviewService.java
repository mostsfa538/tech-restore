package com.techRestore.tech.restore.admin.service;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import com.techRestore.tech.restore.admin.repository.AdminReviewRepository;
import com.techRestore.tech.restore.common.exception.NotFoundException;
import com.techRestore.tech.restore.common.model.entities.Review;
import com.techRestore.tech.restore.common.utils.DTOConverter;
import com.techRestore.tech.restore.user.dto.reviews.ReviewResponseDTO;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AdminReviewService {

    private final AdminReviewRepository reviewRepository;

    @PreAuthorize("hasRole('ADMIN')")
    public Page<ReviewResponseDTO> getAllReviews(Pageable pageable) {
        return reviewRepository.findAll(pageable)
                .map(DTOConverter::toReviewResponseDTO);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public void deleteReviewByAdmin(UUID id) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Review not found with id: " + id));
        reviewRepository.delete(review);
    }
}
