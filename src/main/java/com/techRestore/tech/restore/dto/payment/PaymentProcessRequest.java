package com.techRestore.tech.restore.dto.payment;

import com.techRestore.tech.restore.model.enums.PaymentMethod;

import lombok.Data;

import java.math.BigDecimal;

import java.util.UUID;

@Data

public class PaymentProcessRequest {

    private UUID orderId;

    private UUID repairRequestId;

    private BigDecimal amount;

    private PaymentMethod paymentMethod;

    private String customerEmail;

    private String customerPhone;

    private String customerName;

}
