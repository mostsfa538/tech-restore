package com.techRestore.tech.restore.user.dto.order;

import java.math.BigDecimal;
import java.util.UUID;

import lombok.Data;

@Data
public class OrderItemResponseDTO {
    private UUID id;
    private UUID productId;
    private Integer quantity;
    private BigDecimal priceAtCheckout;
    private UUID shopId;
    private BigDecimal subtotal;
    private String productNAme;
    private BigDecimal price;
    private String shopName;

}
