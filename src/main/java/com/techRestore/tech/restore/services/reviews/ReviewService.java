package com.techRestore.tech.restore.services.reviews;

import java.time.LocalDateTime;
import java.util.UUID;

import com.techRestore.tech.restore.exception.NotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import com.techRestore.tech.restore.dto.reviews.ReviewRequestDTO;
import com.techRestore.tech.restore.dto.reviews.ReviewResponseDTO;
import com.techRestore.tech.restore.model.entities.Review;
import com.techRestore.tech.restore.model.entities.User;
import com.techRestore.tech.restore.repository.ReviewRepository;
import com.techRestore.tech.restore.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;

@Service
@RequiredArgsConstructor
public class ReviewService {
  private final ReviewRepository reviewRepository;
  private final UserRepository userRepository;

  @PreAuthorize("hasRole('ADMIN')")
  public Page<ReviewResponseDTO> getAllReviews(Pageable pageable) {
    return reviewRepository.findAll(pageable)
        .map(this::toResponseDTO);
  }

  @PreAuthorize("hasRole('GUEST')")
  public ReviewResponseDTO createReview(UUID shopId, ReviewRequestDTO reviewRequestDTO) {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    String email = authentication.getName();
    User user = userRepository.findByEmail(email);
    if (user == null) {
      throw new NotFoundException("User not found with email: " + email);
    }

    if (reviewRepository.existsByUserIdAndShopId(user.getId(), shopId)) {
      throw new RuntimeException("You have already submitted a review for this shop.");
    }

    Review review = new Review();
    review.setUserId(user.getId());
    review.setShopId(shopId);
    review.setRating(reviewRequestDTO.getRating());
    review.setComment(reviewRequestDTO.getComment());
    review.setCreatedAt(LocalDateTime.now());

    review = reviewRepository.save(review);
    return toResponseDTO(review);
  }

  public ReviewResponseDTO getReviewById(UUID id) {
    Review review = reviewRepository.findById(id)
        .orElseThrow(() -> new NotFoundException("Review not found with id: " + id));
    return toResponseDTO(review);
  }

  @PreAuthorize("hasRole('GUEST')")
  public ReviewResponseDTO updateReview(UUID reviewId, ReviewRequestDTO reviewRequestDTO) {
    Review review = reviewRepository.findById(reviewId)
        .orElseThrow(() -> new NotFoundException("Review not found with id: " + reviewId));

    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    String email = authentication.getName();
    User user = userRepository.findByEmail(email);

    if (!review.getUserId().equals(user.getId())) {
      throw new RuntimeException("You can only update your own reviews.");
    }

    review.setRating(reviewRequestDTO.getRating());
    review.setComment(reviewRequestDTO.getComment());
    review = reviewRepository.save(review);
    return toResponseDTO(review);
  }

  @PreAuthorize("hasRole('GUEST')")
  public void deleteGuestReview(UUID id) {
    Review review = reviewRepository.findById(id)
        .orElseThrow(() -> new NotFoundException("Review not found with id: " + id));
    
    Authentication authentication=SecurityContextHolder.getContext().getAuthentication();
    String email=authentication.getName();
    User user=userRepository.findByEmail(email);
    if(!review.getUserId().equals(user.getId())){
      throw new RuntimeException("You can only delete your own reviews.");
    }
    reviewRepository.delete(review);
  }

  @PreAuthorize("hasRole('ADMIN')")
  public void deleteReviewByAdmin(UUID id) {
    Review review = reviewRepository.findById(id)
        .orElseThrow(() -> new NotFoundException("Review not found with id: " + id));
    reviewRepository.delete(review);
  }

  public Page<ReviewResponseDTO> getReviewsByShopId(UUID shopId, Pageable pageable) {
    return reviewRepository.findAllByShopId(shopId, pageable)
        .map(this::toResponseDTO);
  }

  private ReviewResponseDTO toResponseDTO(Review review) {
    ReviewResponseDTO dto = new ReviewResponseDTO();
    dto.setId(review.getId());
    dto.setUserId(review.getUserId());
    dto.setShopId(review.getShopId());
    dto.setRating(review.getRating());
    dto.setComment(review.getComment());
    dto.setCreatedAt(review.getCreatedAt());
    return dto;
  }
}
