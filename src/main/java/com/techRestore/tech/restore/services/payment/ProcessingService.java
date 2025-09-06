package com.techRestore.tech.restore.services.payment;

import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.techRestore.tech.restore.exception.CustomException;
import com.techRestore.tech.restore.model.entities.Payment;
import com.techRestore.tech.restore.model.enums.PaymentStatus;
import com.techRestore.tech.restore.repository.PaymentRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Service
@RequiredArgsConstructor
public class ProcessingService {
    private final PaymentRepository paymentRepository;

    @Transactional
    public void processPaymentCallback(Map<String, Object> payload) {
        String success = (String) payload.get("success");
        String paymobOrderId = (String) payload.get("order_id");

        paymentRepository.findByTransactionId(paymobOrderId).ifPresent(payment -> {
            updatePaymentStatus(payment, success);
            paymentRepository.save(payment);
        });

        if (paymentRepository.findByTransactionId(paymobOrderId).isEmpty()) {
            throw new CustomException(HttpStatus.NOT_FOUND, "Payment not found");
        }
    }

    private void updatePaymentStatus(Payment payment, String success) {
        payment.setPaymentStatus("true".equals(success) ? PaymentStatus.COMPLETED : PaymentStatus.FAILED);
    }
}