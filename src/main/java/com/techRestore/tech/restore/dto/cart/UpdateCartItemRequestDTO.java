package com.techRestore.tech.restore.dto.cart;

import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class UpdateCartItemRequestDTO {
    @Min(1)
    private Integer quantity;
}
