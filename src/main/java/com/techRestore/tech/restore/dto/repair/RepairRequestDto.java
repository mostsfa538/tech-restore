package com.techRestore.tech.restore.dto.repair;

import java.util.UUID;

import com.techRestore.tech.restore.model.enums.RepairStatus;

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
        RepairStatus status
) {}