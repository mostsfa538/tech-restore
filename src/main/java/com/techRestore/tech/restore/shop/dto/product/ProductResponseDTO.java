package com.techRestore.tech.restore.shop.dto.product;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import com.techRestore.tech.restore.common.model.enums.ProductCondition;

public record ProductResponseDTO(
                UUID id,
                String name,
                String description,
                BigDecimal price,
                Integer stock,
                String imageUrl,
                ProductCondition condition,
                LocalDateTime createdAt,
                UUID categoryId,
                String categoryName,
                boolean deleted) {
}
