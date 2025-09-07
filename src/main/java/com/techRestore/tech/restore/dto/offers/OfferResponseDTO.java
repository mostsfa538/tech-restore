package com.techRestore.tech.restore.dto.offers;

import com.techRestore.tech.restore.model.enums.DiscountType;
import com.techRestore.tech.restore.model.enums.OfferStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class OfferResponseDTO {
    private UUID id;
    private String name;
    private String description;
    private BigDecimal discountValue;
    private DiscountType discountType;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private OfferStatus status;
    private UUID shopId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}