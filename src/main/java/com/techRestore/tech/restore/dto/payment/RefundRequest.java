package com.techRestore.tech.restore.dto.payment;

import lombok.Data;

import java.math.BigDecimal;

import java.util.UUID;

@Data

public class RefundRequest {

    private UUID paymentId;

    private BigDecimal refundAmount;

}
