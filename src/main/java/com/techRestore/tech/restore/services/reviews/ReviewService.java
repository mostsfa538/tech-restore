package com.techRestore.tech.restore.services.reviews;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
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


  /*
    #### methods ####
    - getAllReviews
    - createReview
    - getReviewById
    - updateReview
    - deleteReview
    - getReviewsByShopId
  */

  @PreAuthorize("hasRole('ADMIN')")
  public List<ReviewResponseDTO> getAllReviews(){
    return reviewRepository.findAll().stream()
        .map(this::toResponseDTO)
        .collect(Collectors.toList());
  }


  @PreAuthorize("hasRole('GUEST')")
  public ReviewResponseDTO createReview(UUID shopId, ReviewRequestDTO reviewRequestDTO) {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    String email = authentication.getName();
    User user = userRepository.findByEmail(email);
    if (user == null) {
        throw new RuntimeException("User not found with email: " + email);
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
        .orElseThrow(() -> new RuntimeException("Review not found with id: " + id));
    return toResponseDTO(review);
  }


  @PreAuthorize("hasRole('GUEST')")
  public ReviewResponseDTO updateReview(UUID reviewId, ReviewRequestDTO reviewRequestDTO) {
    Review review = reviewRepository.findById(reviewId)
        .orElseThrow(() -> new RuntimeException("Review not found with id: " + reviewId));
    

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

  public void deleteReview(UUID id) {
    Review review = reviewRepository.findById(id)
        .orElseThrow(() -> new RuntimeException("Review not found with id: " + id));
    reviewRepository.delete(review);
  }

  public List<ReviewResponseDTO> getReviewsByShopId(UUID shopId) {
    return reviewRepository.findAllByShopId(shopId).stream()
        .map(this::toResponseDTO)
        .collect(Collectors.toList());
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
