package com.techRestore.tech.restore.dto.cart;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import lombok.Data;

@Data
public class CartResponseDTO {
    private UUID id;
    private UUID userId;
    private List<CartItemResponseDTO> items;
    private BigDecimal totalPrice;
    private Integer totalItems;
}
