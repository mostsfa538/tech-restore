package com.techRestore.tech.restore.dto.payment;

import com.techRestore.tech.restore.model.enums.PaymentMethod;

import com.techRestore.tech.restore.model.enums.PaymentStatus;

import lombok.Builder;

import lombok.Data;

import java.math.BigDecimal;

import java.time.LocalDateTime;

import java.util.UUID;

@Data

@Builder

public class PaymentDetailsResponse {

    private UUID paymentId;

    private UUID orderId;

    private UUID repairRequestId;

    private BigDecimal amount;

    private PaymentMethod paymentMethod;

    private PaymentStatus paymentStatus;

    private String paymentReference;

    private String transactionId;

    private LocalDateTime createdAt;

    private LocalDateTime paidAt;

}