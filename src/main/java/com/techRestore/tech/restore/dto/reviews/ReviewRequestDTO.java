package com.techRestore.tech.restore.dto.reviews;


import lombok.Data;
@Data
public class ReviewRequestDTO {
    // private UUID userId;
    // private UUID shopId;
    private int rating;
    private String comment;
}
