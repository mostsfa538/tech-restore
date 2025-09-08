package com.techRestore.tech.restore.shop.dto.offers;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.techRestore.tech.restore.common.model.enums.DiscountType;
import com.techRestore.tech.restore.common.model.enums.OfferStatus;

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
}