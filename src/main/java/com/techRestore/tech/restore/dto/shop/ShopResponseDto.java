package com.techRestore.tech.restore.dto.shop;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Setter
@Getter
public class ShopResponseDto {
    private UUID id;
    private String email;
    private String name;
    private String description;
    private boolean verified;
    private String phone;
    private BigDecimal rating;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
