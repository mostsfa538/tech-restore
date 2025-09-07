package com.techRestore.tech.restore.user.dto.order;

import com.techRestore.tech.restore.common.model.enums.OrderStatus;

import lombok.Data;

@Data
public class OrderStatusUpdateDTO {
  private OrderStatus status;
}
