package com.techRestore.tech.restore.dto.payment;

import lombok.Builder;

import lombok.Data;

import java.util.Map;
import java.util.UUID;

@Data

@Builder

public class PaymobPaymentKeyRequest {

    private String auth_token;

    private Long amount_cents;

    private Integer expiration;

    private Long order_id;

    private Map<String, Object> billing_data;

    private String currency;

    private String integration_id;

}
