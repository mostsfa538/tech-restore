package com.techRestore.tech.restore.dto.payment;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import com.techRestore.tech.restore.model.enums.PaymentMethod;
import com.techRestore.tech.restore.model.enums.PaymentStatus;

import lombok.Data;

@Data
public class PaymentResponseDTO {
  private UUID id;
    private BigDecimal amount;
    private PaymentMethod paymentMethod;
    private PaymentStatus paymentStatus;
    private String paymentReference;
    private LocalDateTime createdAt;
    private LocalDateTime paidAt;
    private UUID orderId;
    private String processingDetails;
}
