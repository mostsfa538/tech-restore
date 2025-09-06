package com.techRestore.tech.restore.services.payment;

import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.techRestore.tech.restore.exception.NotFoundException;
import com.techRestore.tech.restore.model.entities.OrderPayment;
import com.techRestore.tech.restore.model.entities.RepairPayment;
import com.techRestore.tech.restore.model.enums.PaymentStatus;
import com.techRestore.tech.restore.repository.OrderPaymentRepository;
import com.techRestore.tech.restore.repository.RepairPaymentRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProcessingService {
    private final OrderPaymentRepository orderPaymentRepository;
    private final RepairPaymentRepository repairPaymentRepository;

    @Transactional
    public void processPaymentCallback(Map<String, Object> payload) {
        String success = (String) payload.get("success");
        String paymobOrderId = (String) payload.get("order_id");

        orderPaymentRepository.findByPaymentId(paymobOrderId).ifPresent(payment -> {
            updatePaymentStatus(payment, success);
            orderPaymentRepository.save(payment);
        });

        repairPaymentRepository.findByPaymentId(paymobOrderId).ifPresent(payment -> {
            updatePaymentStatus(payment, success);
            repairPaymentRepository.save(payment);
        });

        if (orderPaymentRepository.findByPaymentId(paymobOrderId).isEmpty() &&
            repairPaymentRepository.findByPaymentId(paymobOrderId).isEmpty()) {
            throw new NotFoundException("Payment not found");
        }
    }

    private void updatePaymentStatus(Object payment, String success) {
        if (payment instanceof OrderPayment) {
            OrderPayment orderPayment = (OrderPayment) payment;
            orderPayment.setPaymentStatus("true".equals(success) ? PaymentStatus.COMPLETED : PaymentStatus.FAILED);
        } else if (payment instanceof RepairPayment) {
            RepairPayment repairPayment = (RepairPayment) payment;
            repairPayment.setPaymentStatus("true".equals(success) ? PaymentStatus.COMPLETED : PaymentStatus.FAILED);
        }
    }
}
