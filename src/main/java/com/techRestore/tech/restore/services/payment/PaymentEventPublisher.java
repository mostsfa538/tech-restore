package com.techRestore.tech.restore.services.payment;

import java.math.BigDecimal;
import java.util.UUID;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import com.techRestore.tech.restore.model.enums.PaymentStatus;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentEventPublisher {
    
    private final ApplicationEventPublisher eventPublisher;

    public void publishPaymentStatusChanged(UUID paymentId, PaymentStatus oldStatus, 
                                          PaymentStatus newStatus, BigDecimal amount) {
        PaymentStatusChangedEvent event = new PaymentStatusChangedEvent(
                paymentId, oldStatus, newStatus, amount);
        eventPublisher.publishEvent(event);
        log.info("Published payment status changed event: {} -> {} for payment {}", 
                oldStatus, newStatus, paymentId);
    }
}

