package com.techRestore.tech.restore.services.payment;

import java.math.BigDecimal;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.techRestore.tech.restore.dto.payment.PaymobAuthResponse;
import com.techRestore.tech.restore.dto.payment.PaymobOrderRequest;
import com.techRestore.tech.restore.dto.payment.PaymobOrderResponse;
import com.techRestore.tech.restore.dto.payment.PaymobPaymentKeyRequest;
import com.techRestore.tech.restore.dto.payment.PaymobPaymentKeyResponse;
import com.techRestore.tech.restore.dto.payment.PaymobRefundResponse;
import com.techRestore.tech.restore.security.config.payment.PaymobConfig;

import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;


import org.springframework.http.HttpHeaders;


import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymobService {

    private final PaymobConfig paymobConfig;
    private final RestTemplate restTemplate;

    public String authenticateAndGetToken() {
        try {
            if (paymobConfig.getAuthUrl() == null || paymobConfig.getAuthUrl().trim().isEmpty()) {
                throw new IllegalStateException("Paymob auth URL is not configured");
            }

            URI uri = new URI(paymobConfig.getAuthUrl());
            if (!uri.isAbsolute()) {
                throw new IllegalStateException("Paymob auth URL is not absolute");
            }

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, String> requestBody = new HashMap<>();
            requestBody.put("api_key", paymobConfig.getApiKey());

            HttpEntity<Map<String, String>> entity = new HttpEntity<>(requestBody, headers);

            ResponseEntity<PaymobAuthResponse> response = restTemplate.postForEntity(
                uri, entity, PaymobAuthResponse.class);

            if (response.getStatusCode() == HttpStatus.CREATED && response.getBody() != null) {
                log.info("Paymob authentication successful");
                return response.getBody().getToken();
            }

            log.error("Paymob authentication failed with status: {}, body: {}", 
                response.getStatusCode(), response.getBody());
            throw new RuntimeException("Failed to authenticate with Paymob");

        } catch (HttpClientErrorException e) {
            log.error("HTTP error during Paymob authentication: Status={}, Response={}", 
                e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("Paymob authentication failed: " + e.getMessage(), e);
        } catch (URISyntaxException e) {
            log.error("Invalid Paymob auth URL: {}", paymobConfig.getAuthUrl());
            throw new IllegalStateException("Invalid Paymob auth URL", e);
        } catch (Exception e) {
            log.error("Unexpected error authenticating with Paymob: {}", e.getMessage());
            throw new RuntimeException("Paymob authentication failed", e);
        }
    }

    public PaymobOrderResponse createOrder(String authToken, BigDecimal amount, String merchantOrderId) {
        try {
            if (paymobConfig.getOrderUrl() == null || paymobConfig.getOrderUrl().trim().isEmpty()) {
                throw new IllegalStateException("Paymob order URL is not configured");
            }

            URI uri = new URI(paymobConfig.getOrderUrl());
            if (!uri.isAbsolute()) {
                throw new IllegalStateException("Paymob order URL is not absolute");
            }

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            PaymobOrderRequest orderRequest = PaymobOrderRequest.builder()
                    .auth_token(authToken)
                    .delivery_needed(false)
                    .amount_cents(amount.multiply(BigDecimal.valueOf(100)).longValue())
                    .currency("EGP")
                    .merchant_order_id(merchantOrderId)
                    .build();

            HttpEntity<PaymobOrderRequest> entity = new HttpEntity<>(orderRequest, headers);

            ResponseEntity<PaymobOrderResponse> response = restTemplate.postForEntity(
                uri, entity, PaymobOrderResponse.class);

            if (response.getStatusCode() == HttpStatus.CREATED && response.getBody() != null) {
                log.info("Paymob order created successfully with ID: {}", response.getBody().getId());
                return response.getBody();
            }

            log.error("Paymob order creation failed with status: {}, body: {}, headers: {}", 
                response.getStatusCode(), response.getBody(), response.getHeaders());
            throw new RuntimeException("Failed to create Paymob order");

        } catch (HttpClientErrorException e) {
            log.error("HTTP error during Paymob order creation: Status={}, Response={}", 
                e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("Paymob order creation failed: " + e.getMessage(), e);
        } catch (URISyntaxException e) {
            log.error("Invalid Paymob order URL: {}", paymobConfig.getOrderUrl());
            throw new IllegalStateException("Invalid Paymob order URL", e);
        } catch (Exception e) {
            log.error("Unexpected error creating Paymob order: {}", e.getMessage(), e);
            throw new RuntimeException("Paymob order creation failed", e);
        }
    }

    public String generatePaymentKey(String authToken, BigDecimal amount, Long orderId, 
                                String customerEmail, String customerPhone, String customerName) {
        try {
            if (paymobConfig.getPaymentKeyUrl() == null || paymobConfig.getPaymentKeyUrl().trim().isEmpty()) {
                throw new IllegalStateException("Paymob payment key URL is not configured");
            }

            URI uri = new URI(paymobConfig.getPaymentKeyUrl());
            if (!uri.isAbsolute()) {
                throw new IllegalStateException("Paymob payment key URL is not absolute");
            }

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, Object> billingData = new HashMap<>();
            billingData.put("email", customerEmail);
            billingData.put("phone_number", customerPhone);
            String[] names = customerName.split(" ", 2);
            billingData.put("first_name", names[0]);
            billingData.put("last_name", names.length > 1 ? names[1] : "");
            billingData.put("apartment", "NA");
            billingData.put("floor", "NA");
            billingData.put("street", "NA");
            billingData.put("building", "NA");
            billingData.put("shipping_method", "NA");
            billingData.put("postal_code", "NA");
            billingData.put("city", "Cairo");
            billingData.put("country", "EG");
            billingData.put("state", "NA");

            PaymobPaymentKeyRequest paymentKeyRequest = PaymobPaymentKeyRequest.builder()
                    .auth_token(authToken)
                    .amount_cents(amount.multiply(BigDecimal.valueOf(100)).longValue())
                    .expiration(3600)
                    .order_id(orderId)
                    .billing_data(billingData)
                    .currency("EGP")
                    .integration_id(paymobConfig.getCardIntegrationId())
                    .build();

            HttpEntity<PaymobPaymentKeyRequest> entity = new HttpEntity<>(paymentKeyRequest, headers);

            ResponseEntity<PaymobPaymentKeyResponse> response = restTemplate.postForEntity(
                uri, entity, PaymobPaymentKeyResponse.class);

            if (response.getStatusCode() == HttpStatus.CREATED && response.getBody() != null) {
                log.info("Paymob payment key generated successfully");
                return response.getBody().getToken();
            }

            log.error("Paymob payment key generation failed with status: {}, body: {}", 
                response.getStatusCode(), response.getBody());
            throw new RuntimeException("Failed to generate Paymob payment key");

        } catch (HttpClientErrorException e) {
            log.error("HTTP error during Paymob payment key generation: Status={}, Response={}", 
                e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("Paymob payment key generation failed: " + e.getMessage(), e);
        } catch (URISyntaxException e) {
            log.error("Invalid Paymob payment key URL: {}", paymobConfig.getPaymentKeyUrl());
            throw new IllegalStateException("Invalid Paymob payment key URL", e);
        } catch (Exception e) {
            log.error("Error generating Paymob payment key: {}", e.getMessage());
            throw new RuntimeException("Paymob payment key generation failed", e);
        }
    }

    public String generatePaymentIframeUrl(String paymentToken) {
        return String.format("https://accept.paymob.com/api/acceptance/iframes/%s?payment_token=%s",
                paymobConfig.getIframeId(), paymentToken);
    }

    public void refundTransaction(String authToken, String transactionId, BigDecimal amount) {
        try {
            if (paymobConfig.getRefundUrl() == null || paymobConfig.getRefundUrl().trim().isEmpty()) {
                throw new IllegalStateException("Paymob refund URL is not configured");
            }

            URI uri = new URI(paymobConfig.getRefundUrl());
            if (!uri.isAbsolute()) {
                throw new IllegalStateException("Paymob refund URL is not absolute");
            }

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("auth_token", authToken);
            requestBody.put("transaction_id", transactionId);
            requestBody.put("amount_cents", amount.multiply(BigDecimal.valueOf(100)).longValue());

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            ResponseEntity<PaymobRefundResponse> response = restTemplate.postForEntity(
                uri, entity, PaymobRefundResponse.class);

            if (response.getStatusCode() == HttpStatus.CREATED && response.getBody() != null && response.getBody().isSuccess()) {
                log.info("Paymob refund successful for transaction: {}", transactionId);
                return;
            }

            log.error("Paymob refund failed with status: {}, body: {}", 
                response.getStatusCode(), response.getBody());
            throw new RuntimeException("Failed to refund Paymob transaction");

        } catch (HttpClientErrorException e) {
            log.error("HTTP error during Paymob refund: Status={}, Response={}", 
                e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("Paymob refund failed: " + e.getMessage(), e);
        } catch (URISyntaxException e) {
            log.error("Invalid Paymob refund URL: {}", paymobConfig.getRefundUrl());
            throw new IllegalStateException("Invalid Paymob refund URL", e);
        } catch (Exception e) {
            log.error("Error refunding Paymob transaction: {}", e.getMessage());
            throw new RuntimeException("Paymob refund failed", e);
        }
    }
}