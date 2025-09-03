package com.techRestore.tech.restore.services.payment;

import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

import com.techRestore.tech.restore.dto.payment.PaymentCallbackRequest;
import com.techRestore.tech.restore.model.entities.OrderPayment;
import com.techRestore.tech.restore.model.entities.RepairPayment;
import com.techRestore.tech.restore.model.enums.PaymentStatus;
import com.techRestore.tech.restore.repository.OrderPaymentRepository;
import com.techRestore.tech.restore.repository.RepairPaymentRepository;
import com.techRestore.tech.restore.security.config.payment.PaymobConfig;

import javax.crypto.Mac;

import javax.crypto.spec.SecretKeySpec;

import java.nio.charset.StandardCharsets;

import java.time.LocalDateTime;

import java.util.Optional;

@Service

@RequiredArgsConstructor

@Slf4j

public class PaymentCallbackService {

    

    private final PaymobConfig paymobConfig;

    private final OrderPaymentRepository orderPaymentRepository;

    private final RepairPaymentRepository repairPaymentRepository;

    private final PaymentEventPublisher paymentEventPublisher;

    @Transactional

    public void handlePaymentCallback(PaymentCallbackRequest callback) {

        try {

            if (!verifyHmacSignature(callback)) {

                log.error("Invalid HMAC signature for payment callback");

                throw new SecurityException("Invalid HMAC signature");

            }

            PaymentStatus status = determinePaymentStatus(callback);

            

            updatePaymentFromCallback(callback, status);

            

            log.info("Payment callback processed successfully for order: {}", callback.getOrder_id());

            

        } catch (Exception e) {

            log.error("Error processing payment callback: {}", e.getMessage(), e);

            throw new RuntimeException("Failed to process payment callback", e);

        }

    }

    private boolean verifyHmacSignature(PaymentCallbackRequest callback) {

        try {

            String concatenatedString = String.join("",

                callback.getAmount_cents(),

                callback.getCreated_at(),

                callback.getCurrency(),

                callback.getError_occured(),

                callback.getHas_parent_transaction(),

                callback.getId(),

                callback.getIntegration_id(),

                callback.getIs_3d_secure(),

                callback.getIs_auth(),

                callback.getIs_capture(),

                callback.getIs_refunded(),

                callback.getIs_standalone_payment(),

                callback.getIs_voided(),

                callback.getOrder_id(),

                callback.getOwner(),

                callback.getPending(),

                callback.getSource_data_pan(),

                callback.getSource_data_sub_type(),

                callback.getSource_data_type(),

                callback.getSuccess()

            );

            Mac mac = Mac.getInstance("HmacSHA512");

            SecretKeySpec secretKeySpec = new SecretKeySpec(

                paymobConfig.getHmacSecret().getBytes(StandardCharsets.UTF_8), "HmacSHA512");

            mac.init(secretKeySpec);

            

            byte[] hash = mac.doFinal(concatenatedString.getBytes(StandardCharsets.UTF_8));

            StringBuilder hexString = new StringBuilder();

            for (byte b : hash) {

                String hex = Integer.toHexString(0xff & b);

                if (hex.length() == 1) {

                    hexString.append('0');

                }

                hexString.append(hex);

            }

            

            String calculatedHmac = hexString.toString();

            return calculatedHmac.equals(callback.getHmac());

            

        } catch (Exception e) {

            log.error("Error verifying HMAC signature: {}", e.getMessage());

            return false;

        }

    }

    private PaymentStatus determinePaymentStatus(PaymentCallbackRequest callback) {

        if ("true".equals(callback.getSuccess())) {

            return PaymentStatus.COMPLETED;

        } else if ("true".equals(callback.getPending())) {

            return PaymentStatus.PROCESSING;

        } else if ("true".equals(callback.getError_occured())) {

            return PaymentStatus.FAILED;

        } else {

            return PaymentStatus.FAILED;

        }

    }

    private void updatePaymentFromCallback(PaymentCallbackRequest callback, PaymentStatus status) {

        String orderIdStr = callback.getOrder_id();

        

        Optional<OrderPayment> orderPaymentOpt = orderPaymentRepository.findByPaymentReference(orderIdStr);

        if (orderPaymentOpt.isPresent()) {

            OrderPayment payment = orderPaymentOpt.get();

            PaymentStatus oldStatus = payment.getPaymentStatus();

            payment.setPaymentStatus(status);

            if (status == PaymentStatus.COMPLETED) {

                payment.setPaidAt(LocalDateTime.now());

                payment.setTransactionId(callback.getId());

            }

            orderPaymentRepository.save(payment);

            paymentEventPublisher.publishPaymentStatusChanged(payment.getId(), oldStatus, status, payment.getAmount());

            log.info("Updated order payment status to {} for reference: {}", status, orderIdStr);

            return;

        }

        

        Optional<RepairPayment> repairPaymentOpt = repairPaymentRepository.findByPaymentReference(orderIdStr);

        if (repairPaymentOpt.isPresent()) {

            RepairPayment payment = repairPaymentOpt.get();

            PaymentStatus oldStatus = payment.getPaymentStatus();

            payment.setPaymentStatus(status);

            if (status == PaymentStatus.COMPLETED) {

                payment.setTransactionId(callback.getId());

            }

            repairPaymentRepository.save(payment);

            paymentEventPublisher.publishPaymentStatusChanged(payment.getId(), oldStatus, status, payment.getAmount());

            log.info("Updated repair payment status to {} for reference: {}", status, orderIdStr);

            return;

        }

        

        log.warn("No payment found for reference: {}", orderIdStr);

    }

}
