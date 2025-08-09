package com.techRestore.tech.restore.dto.cart;

import java.util.UUID;

import lombok.Data;

@Data
public class AddToCartRequestDTO {
  private UUID productId;
  private Integer quantity;
}
