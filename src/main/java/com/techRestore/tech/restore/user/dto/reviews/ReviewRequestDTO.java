package com.techRestore.tech.restore.user.dto.reviews;

import java.math.BigDecimal;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import lombok.Data;

@Data
public class ReviewRequestDTO {
    @Min(1)
    @Max(5)
    private BigDecimal rating;
    @NotBlank
    @Size(max = 1000)
    private String comment;
}
