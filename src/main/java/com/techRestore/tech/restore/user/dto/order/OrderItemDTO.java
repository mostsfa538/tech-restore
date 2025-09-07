package com.techRestore.tech.restore.user.dto.order;

import java.math.BigDecimal;
import java.util.UUID;

import lombok.Data;

@Data
public class OrderItemDTO {
  private UUID id;
  private UUID productId;
  private UUID shopId;
  private Integer quantity;
  private BigDecimal priceAtCheckout;
  private String productName;
  private String productImageUrl;
  private String shopName;
}
