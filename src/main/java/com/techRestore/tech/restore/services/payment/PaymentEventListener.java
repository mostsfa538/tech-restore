package com.techRestore.tech.restore.services.payment;

import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class PaymentEventListener {

    @EventListener
    @Async
    public void handlePaymentStatusChanged(PaymentStatusChangedEvent event) {
        log.info("Handling payment status change event for payment: {}", event.getPaymentId());
        
        switch (event.getNewStatus()) {
            case COMPLETED:
                handlePaymentCompleted(event);
                break;
            case FAILED:
                handlePaymentFailed(event);
                break;
            case REFUNDED:
                handlePaymentRefunded(event);
                break;
            default:
                log.debug("No specific handler for status: {}", event.getNewStatus());
        }
    }

    private void handlePaymentCompleted(PaymentStatusChangedEvent event) {
        // Send confirmation email, update order status, etc.
        log.info("Payment completed successfully for payment: {}", event.getPaymentId());
        // Implement notification logic here
    }

    private void handlePaymentFailed(PaymentStatusChangedEvent event) {
        // Send failure notification, log for investigation, etc.
        log.warn("Payment failed for payment: {}", event.getPaymentId());
        // Implement failure handling logic here
    }

    private void handlePaymentRefunded(PaymentStatusChangedEvent event) {
        // Process refund confirmation, update accounting, etc.
        log.info("Payment refunded for payment: {}", event.getPaymentId());
        // Implement refund processing logic here
    }
}

