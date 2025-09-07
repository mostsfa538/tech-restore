package com.techRestore.tech.restore.user.dto.order;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class OrderItemCreateDTO {
    @NotNull(message = "Product ID is required")
    private UUID productId;

    @NotNull(message = "Shop ID is required")
    private UUID shopId;

    @NotNull(message = "Quantity is required")
    @jakarta.validation.constraints.Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantity;
}
