package com.techRestore.tech.restore.dto.product;

import com.techRestore.tech.restore.model.enums.ProductCondition;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record ProductResponseDTO(
        UUID id,
        String name,
        String description,
        BigDecimal price,
        Integer stock,
        String imageUrl,
        ProductCondition condition,
        LocalDateTime createdAt,
        String categoryName
) {
}
