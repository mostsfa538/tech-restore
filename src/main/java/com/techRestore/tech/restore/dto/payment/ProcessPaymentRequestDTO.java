package com.techRestore.tech.restore.dto.payment;

import java.util.UUID;

import com.techRestore.tech.restore.model.enums.PaymentMethod;

import lombok.Data;

@Data
public class ProcessPaymentRequestDTO {
  private UUID orderId;  
    private UUID paymentMethodId;
    private PaymentMethod paymentMethod;
}
