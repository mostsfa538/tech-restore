package com.techRestore.tech.restore.user.dto.order;

import java.util.UUID;

import com.techRestore.tech.restore.common.model.enums.OrderStatus;

import lombok.Data;

@Data
public class TrackingResponseDTO {
  private UUID orderId;
  private OrderStatus status;
}
