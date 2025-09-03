package com.techRestore.tech.restore.services.payment;

import com.techRestore.tech.restore.dto.payment.PaymentProcessRequest;
import com.techRestore.tech.restore.model.enums.PaymentMethod;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@Slf4j
public class PaymentValidationService {

    private static final BigDecimal MIN_PAYMENT_AMOUNT = new BigDecimal("1.00");
    private static final BigDecimal MAX_PAYMENT_AMOUNT = new BigDecimal("50000.00");

    public void validatePaymentRequest(PaymentProcessRequest request) {
        validateAmount(request.getAmount());
        validatePaymentMethod(request.getPaymentMethod());
        validateOrderOrRepairId(request);
        validateCustomerInfo(request);
    }

    private void validateAmount(BigDecimal amount) {
        if (amount == null) {
            throw new IllegalArgumentException("Payment amount is required");
        }
        if (amount.compareTo(MIN_PAYMENT_AMOUNT) < 0) {
            throw new IllegalArgumentException("Payment amount must be at least " + MIN_PAYMENT_AMOUNT);
        }
        if (amount.compareTo(MAX_PAYMENT_AMOUNT) > 0) {
            throw new IllegalArgumentException("Payment amount cannot exceed " + MAX_PAYMENT_AMOUNT);
        }
    }

    private void validatePaymentMethod(PaymentMethod paymentMethod) {
        if (paymentMethod == null) {
            throw new IllegalArgumentException("Payment method is required");
        }
    }

    private void validateOrderOrRepairId(PaymentProcessRequest request) {
        if (request.getOrderId() == null && request.getRepairRequestId() == null) {
            throw new IllegalArgumentException("Either orderId or repairRequestId must be provided");
        }
        if (request.getOrderId() != null && request.getRepairRequestId() != null) {
            throw new IllegalArgumentException("Cannot process payment for both order and repair request simultaneously");
        }
    }

    private void validateCustomerInfo(PaymentProcessRequest request) {
        if (request.getPaymentMethod() == PaymentMethod.CREDIT_CARD) {
            if (request.getCustomerEmail() == null || request.getCustomerEmail().trim().isEmpty()) {
                throw new IllegalArgumentException("Customer email is required for credit card payments");
            }
            if (request.getCustomerPhone() == null || request.getCustomerPhone().trim().isEmpty()) {
                throw new IllegalArgumentException("Customer phone is required for credit card payments");
            }
            if (request.getCustomerName() == null || request.getCustomerName().trim().isEmpty()) {
                throw new IllegalArgumentException("Customer name is required for credit card payments");
            }
        }
    }
}