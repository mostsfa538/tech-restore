package com.techRestore.tech.restore.admin.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import com.techRestore.tech.restore.common.model.enums.PaymentMethod;
import com.techRestore.tech.restore.common.model.enums.PaymentStatus;

import lombok.Data;

@Data
public class SubscriptionDetails {
  private UUID subscriptionId;
  private UUID shopId;
  private String shopName;
  private LocalDateTime startDate;
  private LocalDateTime endDate;
  private int months;
  private PaymentMethod paymentMethod;
  private PaymentStatus paymentStatus;
}
