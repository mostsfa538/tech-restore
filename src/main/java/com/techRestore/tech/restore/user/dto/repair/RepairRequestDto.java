package com.techRestore.tech.restore.user.dto.repair;

import java.math.BigDecimal;
import java.util.UUID;

import com.techRestore.tech.restore.common.model.enums.RepairStatus;

public record RepairRequestDto(
        UUID id,
        UUID deliveryId,
        UUID userId,
        UUID shopId,
        UUID deliveryAddress,
        UUID paymentId,
        String description,
        String deliveryMethod,
        UUID deviceCategory,
        String paymentMethod,
        boolean confirmed,
        BigDecimal price,
        RepairStatus status,
        String shopName,
        String deliveryAddressDetails
        ) {
}