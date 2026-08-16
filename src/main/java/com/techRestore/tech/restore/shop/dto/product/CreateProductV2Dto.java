package com.techRestore.tech.restore.shop.dto.product;

import java.math.BigDecimal;
import java.util.UUID;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.web.multipart.MultipartFile;

import com.techRestore.tech.restore.common.model.enums.ProductCondition;

public record CreateProductV2Dto(
        @NotBlank(message = "Product name cannot be blank") String name,

        @NotBlank(message = "Product description cannot be blank") String description,

        @NotNull(message = "Product price cannot be null") @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0") BigDecimal price,

        @NotNull(message = "Product category ID cannot be null") UUID categoryId,

        @NotNull(message = "Stock quantity cannot be null") @Min(value = 0, message = "Stock quantity cannot be negative") Integer stockQuantity,

        @NotNull(message = "Product condition cannot be null") ProductCondition condition,

        @NotNull(message = "Product image file cannot be null") MultipartFile image) {
}
