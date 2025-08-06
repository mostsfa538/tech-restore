package com.techRestore.tech.restore.dto.product;

import com.techRestore.tech.restore.model.entities.Category;
import com.techRestore.tech.restore.model.enums.ProductCondition;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.URL;

import java.math.BigDecimal;

public record UpdateProductDto(
        @Size(min = 1, message = "Product name cannot be empty if provided")
        String name,

        @Size(min = 1, message = "Product description cannot be empty if provided")
        String description,

        @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
        BigDecimal price,

        @URL(message = "Invalid URL format")
        String imageUrl,

        Category category,

        @Min(value = 0, message = "Stock quantity cannot be negative")
        Integer stockQuantity,

        ProductCondition condition
) {}
