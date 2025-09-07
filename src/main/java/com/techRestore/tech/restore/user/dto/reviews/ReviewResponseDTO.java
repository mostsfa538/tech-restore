package com.techRestore.tech.restore.user.dto.reviews;

import java.time.LocalDateTime;
import java.util.UUID;

import lombok.Data;

@Data
public class ReviewResponseDTO {
    private UUID id;
    private UUID userId;
    private UUID shopId;
    private int rating;
    private String comment;
    private LocalDateTime createdAt;
}
