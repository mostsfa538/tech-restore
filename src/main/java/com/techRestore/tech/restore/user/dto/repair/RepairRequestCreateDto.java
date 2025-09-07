package com.techRestore.tech.restore.user.dto.repair;

import java.util.UUID;

import com.techRestore.tech.restore.common.model.enums.DeliveryMethod;
import com.techRestore.tech.restore.common.model.enums.PaymentMethod;

public record RepairRequestCreateDto(
        UUID deliveryAddress,
        String description,
        DeliveryMethod deliveryMethod,
        UUID deviceCategory,
        PaymentMethod paymentMethod) {
}