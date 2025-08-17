package com.techRestore.tech.restore.dto.repair;

import java.util.UUID;

import com.techRestore.tech.restore.model.enums.DeliveryMethod;
import com.techRestore.tech.restore.model.enums.PaymentMethod;

public record RepairRequestUpdateDto(
    String description,
    UUID deliveryAddressId,
    PaymentMethod paymentMethod,
    DeliveryMethod deliveryMethod,
    UUID categoryId
) {
    
}
