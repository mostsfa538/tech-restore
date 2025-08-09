package com.techRestore.tech.restore.dto.product;

import java.math.BigDecimal;

import com.techRestore.tech.restore.model.entities.Category;
import com.techRestore.tech.restore.model.enums.ProductCondition;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.constraints.URL;

public record CreateProductDto(
        @NotBlank(message = "Product name cannot be blank")
        String name,

        @NotBlank(message = "Product description cannot be blank")
        String description,

        @NotNull(message = "Product price cannot be null")
        @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
        BigDecimal price,

        @NotBlank(message = "Product image URL cannot be blank")
        @URL(message = "Invalid URL format")
        String imageUrl,

        @NotNull(message = "Product category cannot be null")
        Category category,

        @NotNull(message = "Stock quantity cannot be null")
        @Min(value = 0, message = "Stock quantity cannot be negative")
        Integer stockQuantity,

        @NotNull(message = "Product condition cannot be null")
        ProductCondition condition
) {}
