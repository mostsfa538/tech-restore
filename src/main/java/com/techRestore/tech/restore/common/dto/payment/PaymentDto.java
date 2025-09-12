package com.techRestore.tech.restore.common.dto.payment;

import com.techRestore.tech.restore.common.model.enums.PaymentMethod;
import com.techRestore.tech.restore.common.model.enums.PaymentStatus;
import com.techRestore.tech.restore.common.model.enums.PaymentType;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class PaymentDto {
    private UUID id;
    private BigDecimal amount;
    private PaymentMethod paymentMethod;
    private PaymentStatus paymentStatus;
    private PaymentType paymentType;
    private String paymentReference;
    private String transactionId;
    private String details;
    private LocalDateTime paidAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
