package com.techRestore.tech.restore.user.dto.repair;

import java.util.UUID;

import com.techRestore.tech.restore.common.model.enums.DeliveryMethod;
import com.techRestore.tech.restore.common.model.enums.PaymentMethod;

public record UserRepairDetailsDto(
        UUID deliveryAddress,
        DeliveryMethod deliveryMethod,
        PaymentMethod paymentMethod) {
}