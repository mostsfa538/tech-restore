package com.techRestore.tech.restore.dto.payment;

import lombok.Data;

@Data
public class PaymentCallbackRequest {

    private String amount_cents;

    private String created_at;

    private String currency;

    private String error_occured;

    private String has_parent_transaction;

    private String id;

    private String integration_id;

    private String is_3d_secure;

    private String is_auth;

    private String is_capture;

    private String is_refunded;

    private String is_standalone_payment;

    private String is_voided;

    private String order_id;

    private String owner;

    private String pending;

    private String source_data_pan;

    private String source_data_sub_type;

    private String source_data_type;

    private String success;

    private String hmac;

}
