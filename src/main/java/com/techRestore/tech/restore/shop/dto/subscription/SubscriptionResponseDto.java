package com.techRestore.tech.restore.shop.dto.subscription;

import com.techRestore.tech.restore.common.model.enums.SubscriptionType;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class SubscriptionResponseDto {
  private UUID id;
  private UUID shopId;
  private Integer months;
  private BigDecimal baseAmount;
  private BigDecimal totalAmount;
  private SubscriptionType type;
  private LocalDateTime startDate;
  private LocalDateTime endDate;
  private LocalDateTime createdAt;
}