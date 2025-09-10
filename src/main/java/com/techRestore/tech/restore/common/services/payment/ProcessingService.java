package com.techRestore.tech.restore.common.services.payment;

import com.techRestore.tech.restore.common.exception.CustomException;
import com.techRestore.tech.restore.common.model.entities.Payment;
import com.techRestore.tech.restore.common.model.enums.PaymentStatus;
import com.techRestore.tech.restore.common.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;

@Service("paymentProcessingService")
@RequiredArgsConstructor
public class ProcessingService {
    private final PaymentRepository paymentRepository;

    @Transactional
    public void processPaymentCallback(Map<String, Object> payload) {
        String success;
        Map<String, Object> obj = (Map<String, Object>) payload.get("obj");
        if (obj != null && obj.get("success") != null) {
            success = String.valueOf(obj.get("success"));
        } else if (payload.get("success") != null) {
            success = (String) payload.get("success");
        } else {
            throw new CustomException(HttpStatus.BAD_REQUEST, "success field not found in callback payload");
        }

        String paymobOrderId;
        if (obj != null && obj.get("order") != null) {
            Map<String, Object> order = (Map<String, Object>) obj.get("order");
            paymobOrderId = String.valueOf(order.get("id"));
        } else if (payload.get("order_id") != null && !((String) payload.get("order_id")).isEmpty()) {
            paymobOrderId = (String) payload.get("order_id");
        } else {
            throw new CustomException(HttpStatus.BAD_REQUEST, "order_id not found in callback payload");
        }

        if (paymobOrderId == null || paymobOrderId.isEmpty()) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "order_id is empty or null in callback payload");
        }

        Payment payment = paymentRepository.findByTransactionIdForUpdate(paymobOrderId)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND,
                        "Payment not found for transaction ID: " + paymobOrderId));

        if (payment.getPaymentStatus() == PaymentStatus.COMPLETED
                || payment.getPaymentStatus() == PaymentStatus.FAILED) {
            return;
        }

        if ("true".equalsIgnoreCase(success)) {
            payment.setPaymentStatus(PaymentStatus.COMPLETED);
            payment.setPaidAt(LocalDateTime.now());
        } else {
            payment.setPaymentStatus(PaymentStatus.FAILED);
        }
        payment.setUpdatedAt(LocalDateTime.now());
        paymentRepository.saveAndFlush(payment);
    }

    @Transactional
    public void handlePaymentResponse(String success, String orderId, String transactionId) {
        if (orderId == null || orderId.isEmpty()) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "Order ID is empty or null in response");
        }

        Payment payment = paymentRepository.findByTransactionId(orderId)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "Payment not found for transaction ID: " + orderId));

        if (payment.getPaymentStatus() == PaymentStatus.COMPLETED || payment.getPaymentStatus() == PaymentStatus.FAILED) {
            return;
        }

        if ("true".equalsIgnoreCase(success)) {
            payment.setPaymentStatus(PaymentStatus.COMPLETED);
            payment.setPaidAt(LocalDateTime.now());
        } else {
            payment.setPaymentStatus(PaymentStatus.FAILED);
        }
        payment.setUpdatedAt(LocalDateTime.now());
        paymentRepository.saveAndFlush(payment);
    }
}