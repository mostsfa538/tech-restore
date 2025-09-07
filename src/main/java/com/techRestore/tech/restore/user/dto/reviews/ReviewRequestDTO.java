package com.techRestore.tech.restore.user.dto.reviews;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import lombok.Data;

@Data
public class ReviewRequestDTO {
    @Min(1)
    @Max(5)
    private int rating;
    @NotBlank
    @Size(max = 1000)
    private String comment;
}
