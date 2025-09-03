package com.techRestore.tech.restore.dto.payment;

import com.techRestore.tech.restore.model.enums.PaymentStatus;

import lombok.Builder;

import lombok.Data;

import java.util.UUID;

@Data

@Builder

public class PaymentProcessResponse {

    private UUID paymentId;

    private PaymentStatus status;

    private String paymentUrl;

    private String paymentReference;

    private String message;

}
