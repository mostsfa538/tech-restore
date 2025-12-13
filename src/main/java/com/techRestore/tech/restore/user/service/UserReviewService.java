package com.techRestore.tech.restore.user.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.techRestore.tech.restore.common.exception.NotFoundException;
import com.techRestore.tech.restore.common.model.entities.Review;
import com.techRestore.tech.restore.common.model.entities.Shop;
import com.techRestore.tech.restore.common.model.entities.User;
import com.techRestore.tech.restore.common.model.enums.OrderStatus;
import com.techRestore.tech.restore.common.model.enums.RepairStatus;
import com.techRestore.tech.restore.common.utils.DTOConverter;
import com.techRestore.tech.restore.shop.repository.ShopRepository;
import com.techRestore.tech.restore.user.dto.reviews.ReviewRequestDTO;
import com.techRestore.tech.restore.user.dto.reviews.ReviewResponseDTO;
import com.techRestore.tech.restore.user.repository.OrderRepository;
import com.techRestore.tech.restore.user.repository.RepairRequestRepository;
import com.techRestore.tech.restore.user.repository.ReviewRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserReviewService {

    private final ReviewRepository reviewRepository;
    private final OrderRepository orderRepository;
    private final RepairRequestRepository repairRequestRepository;
    private final ShopRepository shopRepository;
    private final AuthUtil authUtil;

    @PreAuthorize("hasRole('GUEST')")
    @Transactional
    public ReviewResponseDTO createReview(UUID shopId, ReviewRequestDTO dto) {

        User user = authUtil.getCurrentUser();

        boolean hasDeliveredOrder = orderRepository.findByUserId(user.getId()).stream()
                .filter(o -> o.getStatus() == OrderStatus.DELIVERED)
                .flatMap(o -> o.getOrderItems().stream())
                .anyMatch(i -> i.getShopId().equals(shopId));

        boolean hasDeliveredRepair = repairRequestRepository.findAllByShopId(shopId, Pageable.unpaged()).stream()
                .anyMatch(r -> r.getUserId().equals(user.getId()) && r.getStatus() == RepairStatus.DEVICE_DELIVERED);

        if (!hasDeliveredOrder && !hasDeliveredRepair) {
            throw new IllegalArgumentException("You can only review a shop after delivery.");
        }

        if (reviewRepository.existsByUserIdAndShopId(user.getId(), shopId)) {
            throw new IllegalArgumentException("You have already submitted a review for this shop.");
        }

        Review review = new Review();
        review.setUserId(user.getId());
        review.setShopId(shopId);
        review.setRating(dto.getRating());
        review.setComment(dto.getComment());
        review.setCreatedAt(LocalDateTime.now());

        reviewRepository.save(review);

        recalculateShopRating(shopId);

        return DTOConverter.toReviewResponseDTO(review);
    }

    @PreAuthorize("hasRole('GUEST')")
    @Transactional
    public ReviewResponseDTO updateReview(UUID reviewId, ReviewRequestDTO dto) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new NotFoundException("Review not found"));

        User user = authUtil.getCurrentUser();
        if (!review.getUserId().equals(user.getId())) {
            throw new IllegalArgumentException("You can only update your review.");
        }

        review.setRating(dto.getRating());
        review.setComment(dto.getComment());
        reviewRepository.save(review);

        recalculateShopRating(review.getShopId());

        return DTOConverter.toReviewResponseDTO(review);
    }

    @PreAuthorize("hasRole('GUEST')")
    @Transactional
    public void deleteGuestReview(UUID reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new NotFoundException("Review not found"));

        User user = authUtil.getCurrentUser();
        if (!review.getUserId().equals(user.getId())) {
            throw new IllegalArgumentException("You can only delete your review.");
        }

        UUID shopId = review.getShopId();
        reviewRepository.delete(review);
        recalculateShopRating(shopId);
    }

    public ReviewResponseDTO getReviewById(UUID reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new NotFoundException("Review not found"));
        return DTOConverter.toReviewResponseDTO(review);
    }

    public Page<ReviewResponseDTO> getReviewsByShopId(UUID shopId, Pageable pageable) {
        Page<Review> reviews = reviewRepository.findAllByShopId(shopId, pageable);
        return reviews.map(DTOConverter::toReviewResponseDTO);
    }

    private void recalculateShopRating(UUID shopId) {
        Shop shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new NotFoundException("Shop not found"));

        long count = reviewRepository.countByShopId(shopId);
        if (count == 0) {
            shop.setRating(BigDecimal.ZERO);
            shopRepository.save(shop);
            return;
        }

        BigDecimal sum = reviewRepository.findAllByShopId(shopId).stream()
                .map(Review::getRating)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal average = sum.divide(BigDecimal.valueOf(count), 2, BigDecimal.ROUND_HALF_UP);
        shop.setRating(average);
        shopRepository.save(shop);
    }
}
