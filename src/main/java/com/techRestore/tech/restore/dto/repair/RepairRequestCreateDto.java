package com.techRestore.tech.restore.dto.repair;

import com.techRestore.tech.restore.model.enums.DeliveryMethod;
import com.techRestore.tech.restore.model.enums.PaymentMethod;

import java.util.UUID;

public record RepairRequestCreateDto(
        UUID shopId,
        UUID deliveryAddress,
        String description,
        DeliveryMethod deliveryMethod,
        UUID deviceCategory,
        PaymentMethod paymentMethod
) {
}