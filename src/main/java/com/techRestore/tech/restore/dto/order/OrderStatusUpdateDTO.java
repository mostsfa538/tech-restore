package com.techRestore.tech.restore.dto.order;

import com.techRestore.tech.restore.model.enums.OrderStatus;

import lombok.Data;

@Data
public class OrderStatusUpdateDTO {
  private OrderStatus status;
}
