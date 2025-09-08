package com.techRestore.tech.restore.user.controller;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.techRestore.tech.restore.user.dto.reviews.ReviewRequestDTO;
import com.techRestore.tech.restore.user.dto.reviews.ReviewResponseDTO;
import com.techRestore.tech.restore.user.service.reviews.ReviewService;

import lombok.RequiredArgsConstructor;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PutMapping;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {

  private final ReviewService reviewService;

  @PostMapping("/{shopId}")
  @PreAuthorize("hasRole('GUEST')")
  public ResponseEntity<ReviewResponseDTO> createReview(
      @RequestBody @Valid ReviewRequestDTO reviewRequestDTO,
      @PathVariable UUID shopId) {
    return ResponseEntity.ok(reviewService.createReview(shopId, reviewRequestDTO));
  }

  @GetMapping("/{id}")
  @PreAuthorize("hasRole('GUEST')")
  public ResponseEntity<ReviewResponseDTO> getReviewById(@PathVariable UUID id) {
    return ResponseEntity.ok(reviewService.getReviewById(id));
  }

  @GetMapping("/{shopId}/reviews")
  public ResponseEntity<Page<ReviewResponseDTO>> getReviewByShopId(@PathVariable UUID shopId, Pageable pageable) {
    return ResponseEntity.ok(reviewService.getReviewsByShopId(shopId, pageable));
  }

  @PutMapping("/{id}")
  @PreAuthorize("hasRole('GUEST')")
  public ResponseEntity<ReviewResponseDTO> updateReview(
      @PathVariable UUID id,
      @RequestBody @Valid ReviewRequestDTO reviewRequestDTO) {
    return ResponseEntity.ok(reviewService.updateReview(id, reviewRequestDTO));
  }

  @DeleteMapping("/cancel/{id}")
  public ResponseEntity<Void> deleteReview(@PathVariable UUID id) {
    reviewService.deleteGuestReview(id);
    return ResponseEntity.noContent().build();
  }
}
