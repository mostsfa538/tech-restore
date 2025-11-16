package com.techRestore.tech.restore.admin.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.techRestore.tech.restore.admin.dto.SubscriptionDetails;
import com.techRestore.tech.restore.common.dto.payment.PaymentDto;
import com.techRestore.tech.restore.common.exception.CustomException;
import com.techRestore.tech.restore.common.exception.NotFoundException;
import com.techRestore.tech.restore.common.model.entities.Payment;
import com.techRestore.tech.restore.common.model.entities.Shop;
import com.techRestore.tech.restore.common.model.entities.Subscription;
import com.techRestore.tech.restore.common.model.enums.PaymentMethod;
import com.techRestore.tech.restore.common.model.enums.PaymentStatus;
import com.techRestore.tech.restore.common.model.enums.PaymentType;
import com.techRestore.tech.restore.common.model.enums.SubscriptionType;
import com.techRestore.tech.restore.common.repository.PaymentRepository;
import com.techRestore.tech.restore.shop.repository.ShopRepository;
import com.techRestore.tech.restore.shop.repository.SubscriptionRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AdminSubscriptionService {
  
  private final PaymentRepository paymentRepository;
  private final SubscriptionRepository subscriptionRepository;
  private final ShopRepository shopRepository;

  @Transactional
  public String confirmSubscriptionCashPayment(UUID paymentId){
      Payment payment = paymentRepository.findById(paymentId)
          .orElseThrow(() -> new NotFoundException("Payment not found"));

      if(payment.getPaymentMethod() != PaymentMethod.CASH){
          throw new IllegalArgumentException("Payment method is not cash");
      }
      if(payment.getPaymentStatus() == PaymentStatus.COMPLETED){
          throw new IllegalArgumentException("Payment is already confirmed");
      }

      createSubscriptionFromPayment(payment);

      payment.setPaymentStatus(PaymentStatus.COMPLETED);
      payment.setPaidAt(LocalDateTime.now());
      paymentRepository.saveAndFlush(payment);

      return "Payment confirmed and subscription created successfully";
  }



  @Transactional
  public String RejectSubscriptionCashPayment(UUID paymentId){
    Payment payment = paymentRepository.findById(paymentId)
        .orElseThrow(() -> new NotFoundException("Payment not found"));

    if(payment.getPaymentMethod() != PaymentMethod.CASH){
      throw new IllegalArgumentException("Payment method is not cash");
    }
    if(payment.getPaymentStatus().name() == "FAILED"){
      throw new IllegalArgumentException("Payment is already FAILED");
    }
    // createSubscriptionFromPayment(payment);
    payment.setPaymentStatus(PaymentStatus.FAILED);
    paymentRepository.save(payment);
    return "Payment Rejected and subscription created successfully";
  }

  @Transactional
  public List<SubscriptionDetails> getSubscriptionsByShop(UUID shopId) {
      shopRepository.findById(shopId)
              .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "Shop not found"));
      List<Subscription> subscriptions = subscriptionRepository.findAllByShopIdOrderByStartDateDesc(shopId);
      return subscriptions.stream()
              .map(this::mapToDto)
              .collect(Collectors.toList());
  }


  @Transactional
  public SubscriptionDetails getSubscription(UUID subscriptionId) {
    Subscription sub = subscriptionRepository.findById(subscriptionId)
            .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "Subscription not found"));

    return mapToDto(sub);
  }



  @Transactional
  public Page<SubscriptionDetails> getAllSubscriptionsWithPayment(Pageable pageable) {
    Page<Subscription> subscriptions = subscriptionRepository.findAllWithPayment(pageable);
    return subscriptions.map(this::mapToDto);
  }


  @Transactional
  public Page<PaymentDto> getPendingCashPayments(Pageable pageable) {
      return paymentRepository
              .findAllByPaymentMethodAndPaymentStatusAndPaymentType(PaymentMethod.CASH, PaymentStatus.PENDING, PaymentType.SUBSCRIPTION_PAYMENT,pageable)
              .map(this::mapPaymentToDto);
  }

  private SubscriptionDetails mapToDto(Subscription sub) {
      SubscriptionDetails dto = new SubscriptionDetails();
      dto.setSubscriptionId(sub.getId());
      dto.setShopId(sub.getShopId());
      dto.setShopName(sub.getShop().getName());
      dto.setStartDate(sub.getStartDate());
      dto.setEndDate(sub.getEndDate());
      dto.setMonths(sub.getMonths());
      dto.setPaymentMethod(sub.getPayment() != null ? sub.getPayment().getPaymentMethod() : null);
      dto.setPaymentStatus(sub.getPayment() != null ? sub.getPayment().getPaymentStatus() : null);
      return dto;
  }

  private PaymentDto mapPaymentToDto(Payment payment) {
        PaymentDto dto = new PaymentDto();
        dto.setId(payment.getId());
        dto.setAmount(payment.getAmount());
        dto.setPaymentMethod(payment.getPaymentMethod());
        dto.setPaymentStatus(payment.getPaymentStatus());
        dto.setPaymentType(payment.getPaymentType());
        dto.setPaymentReference(payment.getPaymentReference());
        dto.setTransactionId(payment.getTransactionId());
        dto.setDetails(payment.getDetails());
        dto.setPaidAt(payment.getPaidAt());
        dto.setCreatedAt(payment.getCreatedAt());
        dto.setUpdatedAt(payment.getUpdatedAt());
        return dto;
  }

  private void createSubscriptionFromPayment(Payment payment) {
    boolean exists = subscriptionRepository.existsByPaymentId(payment.getId());
    if (exists) return;

    UUID shopId = payment.getShop().getId();
    Subscription lastSubscription = subscriptionRepository
        .findFirstByShopIdOrderByCreatedAtDesc(shopId)
        .orElse(null);

    int months = payment.getAmount()
            .divide(BigDecimal.valueOf(1000), 0, BigDecimal.ROUND_DOWN)
            .intValue();

    LocalDateTime startDate = (lastSubscription != null && lastSubscription.getEndDate().isAfter(LocalDateTime.now()))
            ? lastSubscription.getEndDate()
            : LocalDateTime.now();
    LocalDateTime endDate = startDate.plusMonths(months);

    Subscription subscription = new Subscription();
    subscription.setShopId(shopId);
    subscription.setPaymentId(payment.getId());
    subscription.setMonths(months);
    subscription.setType(SubscriptionType.COMMISSION);
    subscription.setStartDate(startDate);
    subscription.setEndDate(endDate);
    subscriptionRepository.save(subscription);

    Shop shop = payment.getShop();
    shop.setActivate(true);
    shop.setSubscriptionMonths(months);
    shop.setSubscriptionStartDate(subscription.getStartDate());
    shop.setSubscriptionEndDate(subscription.getEndDate());
    shopRepository.save(shop);
  }

}
