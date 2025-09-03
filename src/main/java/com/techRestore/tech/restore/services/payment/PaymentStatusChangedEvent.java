package com.techRestore.tech.restore.services.payment;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import com.techRestore.tech.restore.model.enums.PaymentStatus;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PaymentStatusChangedEvent {
    private UUID paymentId;
    private PaymentStatus oldStatus;
    private PaymentStatus newStatus;
    private BigDecimal amount;
    private LocalDateTime timestamp;

    public PaymentStatusChangedEvent(UUID paymentId, PaymentStatus oldStatus, 
                                   PaymentStatus newStatus, BigDecimal amount) {
        this.paymentId = paymentId;
        this.oldStatus = oldStatus;
        this.newStatus = newStatus;
        this.amount = amount;
        this.timestamp = LocalDateTime.now();
    }
}
