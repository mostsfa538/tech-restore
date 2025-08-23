package com.techRestore.tech.restore.dto.payment;

import com.techRestore.tech.restore.model.enums.PaymentMethod;

import lombok.Data;

@Data
public class UserPaymentMethodRequestDTO {
    private PaymentMethod paymentMethod;
    private String details;
    private boolean isDefault;
}
