package com.techRestore.tech.restore.controller.reviews;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.techRestore.tech.restore.dto.reviews.ReviewRequestDTO;
import com.techRestore.tech.restore.dto.reviews.ReviewResponseDTO;
import com.techRestore.tech.restore.services.reviews.ReviewService;

import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PutMapping;





@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ReviewController {
  private final ReviewService reviewService;


  /*
    #### endpoints ####
    - getAllReviews (only admin)
    - createReview (only guest)
    - getReviewById
    - updateReview
    - deleteReview
    - getReviewsByShopId
  */


  @GetMapping("/reviews")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<List<ReviewResponseDTO>>getAllReviews(){
    return ResponseEntity.ok(reviewService.getAllReviews());
  }

  @PostMapping("/shops/{shopId}/reviews")
  @PreAuthorize("hasRole('GUEST')")
  public ResponseEntity<ReviewResponseDTO> createReview(@RequestBody ReviewRequestDTO reviewRequestDTO,@PathVariable UUID shopId) {
    return ResponseEntity.ok(reviewService.createReview(shopId,reviewRequestDTO));
  }

  @GetMapping("reviews/{id}")
  public ResponseEntity<ReviewResponseDTO> getReviewById(@PathVariable UUID id) {
    return ResponseEntity.ok(reviewService.getReviewById(id));
  }


  @PutMapping("reviews/{id}")
  @PreAuthorize("hasRole('GUEST')")
  public ResponseEntity<ReviewResponseDTO> updateReview(@PathVariable UUID id, @RequestBody ReviewRequestDTO reviewRequestDTO) {
    return ResponseEntity.ok(reviewService.updateReview(id, reviewRequestDTO));
  }

  @DeleteMapping("reviews/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<Void> deleteReview(@PathVariable UUID id) {
    reviewService.deleteReview(id);
    return ResponseEntity.noContent().build();
  }


  @GetMapping("/shops/{shopId}/reviews")
    public ResponseEntity<List<ReviewResponseDTO>> getReviewsByShopId(@PathVariable UUID shopId) {
      return ResponseEntity.ok(reviewService.getReviewsByShopId(shopId));
  }

 

  
}
