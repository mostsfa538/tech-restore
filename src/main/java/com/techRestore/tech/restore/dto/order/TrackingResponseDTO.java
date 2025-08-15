package com.techRestore.tech.restore.dto.order;

import java.util.UUID;

import com.techRestore.tech.restore.model.enums.OrderStatus;

import lombok.Data;

@Data
public class TrackingResponseDTO {
  private UUID orderId;
  private OrderStatus status;
}
