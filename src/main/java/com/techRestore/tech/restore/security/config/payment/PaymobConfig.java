package com.techRestore.tech.restore.security.config.payment;


import org.springframework.boot.context.properties.ConfigurationProperties;

import org.springframework.context.annotation.Configuration;

import lombok.Data;

@Configuration

@ConfigurationProperties(prefix = "paymob")

@Data

public class PaymobConfig {

    private String apiKey;

    private String authUrl;

    private String orderUrl;

    private String paymentKeyUrl;

    private String iframeId;

    private String cardIntegrationId;

    private String hmacSecret;

    private String refundUrl;

}