package com.techRestore.tech.restore.user.service.reviews;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.techRestore.tech.restore.common.exception.NotFoundException;
import com.techRestore.tech.restore.common.model.entities.Review;
import com.techRestore.tech.restore.common.model.entities.User;
import com.techRestore.tech.restore.common.model.enums.OrderStatus;
import com.techRestore.tech.restore.common.model.enums.RepairStatus;
import com.techRestore.tech.restore.common.utils.DTOConverter;
import com.techRestore.tech.restore.user.dto.reviews.ReviewRequestDTO;
import com.techRestore.tech.restore.user.dto.reviews.ReviewResponseDTO;
import com.techRestore.tech.restore.user.repository.OrderRepository;
import com.techRestore.tech.restore.user.repository.RepairRequestRepository;
import com.techRestore.tech.restore.user.repository.ReviewRepository;
import com.techRestore.tech.restore.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;

@Service
@RequiredArgsConstructor
public class ReviewService {
  private final ReviewRepository reviewRepository;
  private final UserRepository userRepository;
  private final OrderRepository orderRepository;
  private final RepairRequestRepository repairRequestRepository;

  @PreAuthorize("hasRole('GUEST')")
  @Transactional
  public ReviewResponseDTO createReview(UUID shopId, ReviewRequestDTO reviewRequestDTO) {

    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    String email = authentication.getName();
    User user = userRepository.findByEmail(email);
    if (user == null) {
      throw new NotFoundException("User not found with email: " + email);
    }

    boolean hasDeliveredOrder = orderRepository.findByUserId(user.getId()).stream()
        .filter(order -> order.getStatus() == OrderStatus.DELIVERED)
        .flatMap(order -> order.getOrderItems().stream())
        .anyMatch(item -> item.getShopId().equals(shopId));

    boolean hasDeliveredRepair = repairRequestRepository.findAllByShopId(shopId, Pageable.unpaged())
        .stream()
        .anyMatch(request -> request.getUserId().equals(user.getId())
            && request.getStatus() == RepairStatus.DEVICE_DELIVERED);

    if (!hasDeliveredOrder && !hasDeliveredRepair) {
      throw new IllegalArgumentException(
          "You can only review a shop after an order or repair has been delivered.");
    }

    if (reviewRepository.existsByUserIdAndShopId(user.getId(), shopId)) {
      throw new IllegalArgumentException("You have already submitted a review for this shop.");
    }

    Review review = new Review();
    review.setUserId(user.getId());
    review.setShopId(shopId);
    review.setRating(reviewRequestDTO.getRating());
    review.setComment(reviewRequestDTO.getComment());
    review.setCreatedAt(LocalDateTime.now());

    review = reviewRepository.save(review);
    return DTOConverter.toReviewResponseDTO(review);
  }

  public ReviewResponseDTO getReviewById(UUID id) {
    Review review = reviewRepository.findById(id)
        .orElseThrow(() -> new NotFoundException("Review not found with id: " + id));
    return DTOConverter.toReviewResponseDTO(review);
  }

  @PreAuthorize("hasRole('GUEST')")
  public ReviewResponseDTO updateReview(UUID reviewId, ReviewRequestDTO reviewRequestDTO) {
    Review review = reviewRepository.findById(reviewId)
        .orElseThrow(() -> new NotFoundException("Review not found with id: " + reviewId));

    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    String email = authentication.getName();
    User user = userRepository.findByEmail(email);

    if (!review.getUserId().equals(user.getId())) {
      throw new IllegalArgumentException("You can only update your own reviews.");
    }

    review.setRating(reviewRequestDTO.getRating());
    review.setComment(reviewRequestDTO.getComment());
    review = reviewRepository.save(review);
    return DTOConverter.toReviewResponseDTO(review);
  }

  public Page<ReviewResponseDTO> getReviewsByShopId(UUID shopId, Pageable pageable) {
    Page<Review> reviews = reviewRepository.findAllByShopId(shopId, pageable);
    return reviews.map(DTOConverter::toReviewResponseDTO);
  }

  @PreAuthorize("hasRole('GUEST')")
  public void deleteGuestReview(UUID id) {
    Review review = reviewRepository.findById(id)
        .orElseThrow(() -> new NotFoundException("Review not found with id: " + id));

    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    String email = authentication.getName();
    User user = userRepository.findByEmail(email);
    if (!review.getUserId().equals(user.getId())) {
      throw new IllegalArgumentException("You can only delete your own reviews.");
    }
    reviewRepository.delete(review);
  }
}
