package com.techRestore.tech.restore.delivery.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import com.techRestore.tech.restore.common.model.enums.OrderStatus;

import lombok.Data;

@Data
public class OrderDeliveryDto {
  private UUID id;
  private UUID userId;
  private UUID shopId;
  private UUID deliveryId;
  private OrderStatus status;
  private BigDecimal totalPrice;
  private String paymentMethod;
  private LocalDateTime createdAt;
}
