package com.techRestore.tech.restore.dto.payment;

import java.time.LocalDateTime;
import java.util.UUID;

import com.techRestore.tech.restore.model.enums.PaymentMethod;

import lombok.Data;

@Data
public class UserPaymentMethodResponseDTO {
    private UUID id;
    private PaymentMethod paymentMethod;
    private String details;
    private boolean isDefault;
    private LocalDateTime createdAt;
}
