package com.techRestore.tech.restore.dto.cart;

import java.util.UUID;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AddToCartRequestDTO {
    @NotNull
    private UUID productId;
    @NotNull
    @Min(1)
    private Integer quantity;
}

