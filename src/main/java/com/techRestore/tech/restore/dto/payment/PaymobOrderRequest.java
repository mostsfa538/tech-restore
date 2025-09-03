package com.techRestore.tech.restore.dto.payment;

import lombok.Builder;

import lombok.Data;

@Data

@Builder

public class PaymobOrderRequest {

    private String auth_token;

    private boolean delivery_needed;

    private Long amount_cents;

    private String currency;

    private String merchant_order_id;

}
