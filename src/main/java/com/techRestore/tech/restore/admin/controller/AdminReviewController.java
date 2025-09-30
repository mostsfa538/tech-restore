package com.techRestore.tech.restore.admin.controller;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.techRestore.tech.restore.admin.service.AdminReviewService;
import com.techRestore.tech.restore.user.dto.reviews.ReviewResponseDTO;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/admin/reviews")
@RequiredArgsConstructor
public class AdminReviewController {
    private final AdminReviewService reviewService;

    @GetMapping
    public ResponseEntity<Page<ReviewResponseDTO>> getAllReviews(
            Pageable pageable) {
        return ResponseEntity.ok(reviewService.getAllReviews(pageable));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReview(@PathVariable UUID id) {
        reviewService.deleteReviewByAdmin(id);
        return ResponseEntity.noContent().build();
    }
}
