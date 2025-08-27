package com.techRestore.tech.restore.controller.reviews;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.techRestore.tech.restore.dto.reviews.ReviewRequestDTO;
import com.techRestore.tech.restore.dto.reviews.ReviewResponseDTO;
import com.techRestore.tech.restore.services.reviews.ReviewService;

import lombok.RequiredArgsConstructor;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
      @RequestBody ReviewRequestDTO reviewRequestDTO,
      @PathVariable UUID shopId) {
    return ResponseEntity.ok(reviewService.createReview(shopId, reviewRequestDTO));
  }

  @GetMapping("/{id}")
  @PreAuthorize("hasRole('GUEST')")
  public ResponseEntity<ReviewResponseDTO> getReviewById(@PathVariable UUID id) {
    return ResponseEntity.ok(reviewService.getReviewById(id));
  }

  @PutMapping("/{id}")
  @PreAuthorize("hasRole('GUEST')")
  public ResponseEntity<ReviewResponseDTO> updateReview(
      @PathVariable UUID id,
      @RequestBody ReviewRequestDTO reviewRequestDTO) {
    return ResponseEntity.ok(reviewService.updateReview(id, reviewRequestDTO));
  }

  @GetMapping("/{shopId}")
  public ResponseEntity<Page<ReviewResponseDTO>> getReviewsByShopId(
      @PathVariable UUID shopId,
      Pageable pageable) {
    return ResponseEntity.ok(reviewService.getReviewsByShopId(shopId, pageable));
  }
}
