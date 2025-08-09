package com.techRestore.tech.restore.dto.cart;

import java.math.BigDecimal;
import java.util.UUID;

import lombok.Data;

@Data
public class CartItemResponseDTO {
    private UUID id;
    private UUID productId;
    private String productName;
    private BigDecimal productPrice;
    private Integer quantity;
    private BigDecimal subtotal;
}