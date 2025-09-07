package com.techRestore.tech.restore.dto.offers;

import com.techRestore.tech.restore.model.enums.DiscountType;
import com.techRestore.tech.restore.model.enums.OfferStatus;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class OfferRequestDTO {
    @NotBlank
    private String name;

    @NotBlank
    private String description;

    private BigDecimal discountValue;
    private DiscountType discountType;
    private OfferStatus status;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private UUID shopId;
}