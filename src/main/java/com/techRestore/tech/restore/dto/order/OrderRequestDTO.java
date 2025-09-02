package com.techRestore.tech.restore.dto.order;

import java.util.UUID;

import com.techRestore.tech.restore.model.enums.PaymentMethod;


import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class OrderRequestDTO {
    @NotNull
    private UUID deliveryAddressId;
    @NotNull
    private PaymentMethod paymentMethod;
}
