package com.techRestore.tech.restore.dto.shop;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record StockUpdateRequest(@NotNull @Min(0) Integer newStock) {
}
