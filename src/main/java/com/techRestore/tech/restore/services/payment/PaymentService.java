package com.techRestore.tech.restore.services.payment;

import com.techRestore.tech.restore.dto.payment.PaymentDetailsResponse;
import com.techRestore.tech.restore.dto.payment.PaymentProcessRequest;
import com.techRestore.tech.restore.dto.payment.PaymentProcessResponse;
import com.techRestore.tech.restore.dto.payment.PaymobOrderResponse;
import com.techRestore.tech.restore.dto.payment.RefundRequest;
import com.techRestore.tech.restore.model.entities.OrderPayment;
import com.techRestore.tech.restore.model.entities.RepairPayment;
import com.techRestore.tech.restore.model.enums.PaymentMethod;
import com.techRestore.tech.restore.model.enums.PaymentStatus;
import com.techRestore.tech.restore.repository.OrderPaymentRepository;
import com.techRestore.tech.restore.repository.RepairPaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final PaymobService paymobService;
    private final OrderPaymentRepository orderPaymentRepository;
    private final RepairPaymentRepository repairPaymentRepository;
    private final PaymentValidationService paymentValidationService;
    private final PaymentEventPublisher paymentEventPublisher;

    @Transactional
    public PaymentProcessResponse processPayment(PaymentProcessRequest request) {
        try {
            log.info("Processing payment request: {}", request);
            paymentValidationService.validatePaymentRequest(request);

            if (request.getPaymentMethod() == PaymentMethod.CREDIT_CARD) {
                return processCreditCardPayment(request);
            } else {
                return processCashPayment(request);
            }
        } catch (Exception e) {
            log.error("Error processing payment: {}", e.getMessage(), e);
            return PaymentProcessResponse.builder()
                    .status(PaymentStatus.FAILED)
                    .message("Payment processing failed: " + e.getMessage())
                    .build();
        }
    }

    private PaymentProcessResponse processCreditCardPayment(PaymentProcessRequest request) {
        try {
            String authToken = paymobService.authenticateAndGetToken();
            String merchantOrderId = generateMerchantOrderId(request);
            log.info("Generated merchantOrderId: {}", merchantOrderId);

            PaymobOrderResponse paymobOrder = paymobService.createOrder(authToken, request.getAmount(), merchantOrderId);
            log.info("Paymob order created with ID: {}", paymobOrder.getId());

            String paymentToken = paymobService.generatePaymentKey(
                    authToken,
                    request.getAmount(),
                    paymobOrder.getId(), // Use Long directly
                    request.getCustomerEmail(),
                    request.getCustomerPhone(),
                    request.getCustomerName()
            );

            String paymentUrl = paymobService.generatePaymentIframeUrl(paymentToken);
            UUID paymentId = savePaymentRecord(request, String.valueOf(paymobOrder.getId()));

            log.info("Credit card payment initiated successfully for payment ID: {}", paymentId);

            return PaymentProcessResponse.builder()
                    .paymentId(paymentId)
                    .status(PaymentStatus.PENDING)
                    .paymentUrl(paymentUrl)
                    .paymentReference(String.valueOf(paymobOrder.getId()))
                    .message("Payment initiated successfully. Please complete payment using the provided URL.")
                    .build();
        } catch (Exception e) {
            log.error("Error processing credit card payment: {}", e.getMessage(), e);
            throw new RuntimeException("Credit card payment processing failed", e);
        }
    }

    private PaymentProcessResponse processCashPayment(PaymentProcessRequest request) {
        UUID paymentId = savePaymentRecord(request, null);
        log.info("Cash payment registered for payment ID: {}", paymentId);

        return PaymentProcessResponse.builder()
                .paymentId(paymentId)
                .status(PaymentStatus.PENDING)
                .message("Cash payment registered. Payment will be completed upon delivery/service completion.")
                .build();
    }

    private UUID savePaymentRecord(PaymentProcessRequest request, String paymentReference) {
        if (request.getOrderId() != null) {
            return saveOrderPayment(request, paymentReference);
        } else if (request.getRepairRequestId() != null) {
            return saveRepairPayment(request, paymentReference);
        } else {
            throw new IllegalArgumentException("Either orderId or repairRequestId must be provided");
        }
    }

    private UUID saveOrderPayment(PaymentProcessRequest request, String paymentReference) {
        OrderPayment payment = new OrderPayment();
        payment.setOrderId(request.getOrderId());
        payment.setAmount(request.getAmount());
        payment.setPaymentMethod(request.getPaymentMethod());
        payment.setPaymentStatus(PaymentStatus.PENDING);
        payment.setPaymentReference(paymentReference);
        payment.setCreatedAt(LocalDateTime.now());

        OrderPayment savedPayment = orderPaymentRepository.save(payment);
        log.info("Saved order payment with ID: {}", savedPayment.getId());
        return savedPayment.getId();
    }

    private UUID saveRepairPayment(PaymentProcessRequest request, String paymentReference) {
        RepairPayment payment = new RepairPayment();
        payment.setRepairRequestId(request.getRepairRequestId());
        payment.setAmount(request.getAmount());
        payment.setPaymentMethod(request.getPaymentMethod());
        payment.setPaymentStatus(PaymentStatus.PENDING);
        payment.setPaymentReference(paymentReference);
        payment.setCreatedAt(LocalDateTime.now());

        RepairPayment savedPayment = repairPaymentRepository.save(payment);
        log.info("Saved repair payment with ID: {}", savedPayment.getId());
        return savedPayment.getId();
    }

    private String generateMerchantOrderId(PaymentProcessRequest request) {
        String prefix = request.getOrderId() != null ? "ORDER_" : "REPAIR_";
        String id = request.getOrderId() != null ? 
                request.getOrderId().toString() : 
                request.getRepairRequestId().toString();
        return prefix + id + "_" + System.currentTimeMillis();
    }

    public PaymentDetailsResponse getPaymentDetails(UUID paymentId) {
        Optional<OrderPayment> orderPayment = orderPaymentRepository.findById(paymentId);
        if (orderPayment.isPresent()) {
            OrderPayment payment = orderPayment.get();
            return PaymentDetailsResponse.builder()
                    .paymentId(payment.getId())
                    .orderId(payment.getOrderId())
                    .amount(payment.getAmount())
                    .paymentMethod(payment.getPaymentMethod())
                    .paymentStatus(payment.getPaymentStatus())
                    .paymentReference(payment.getPaymentReference())
                    .transactionId(payment.getTransactionId())
                    .createdAt(payment.getCreatedAt())
                    .paidAt(payment.getPaidAt())
                    .build();
        }

        Optional<RepairPayment> repairPayment = repairPaymentRepository.findById(paymentId);
        if (repairPayment.isPresent()) {
            RepairPayment payment = repairPayment.get();
            return PaymentDetailsResponse.builder()
                    .paymentId(payment.getId())
                    .repairRequestId(payment.getRepairRequestId())
                    .amount(payment.getAmount())
                    .paymentMethod(payment.getPaymentMethod())
                    .paymentStatus(payment.getPaymentStatus())
                    .paymentReference(payment.getPaymentReference())
                    .transactionId(payment.getTransactionId())
                    .createdAt(payment.getCreatedAt())
                    .build();
        }

        throw new RuntimeException("Payment not found with ID: " + paymentId);
    }

    public PaymentDetailsResponse getOrderPaymentDetails(UUID orderId) {
        OrderPayment payment = orderPaymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new RuntimeException("Payment not found for order: " + orderId));
        return PaymentDetailsResponse.builder()
                .paymentId(payment.getId())
                .orderId(payment.getOrderId())
                .amount(payment.getAmount())
                .paymentMethod(payment.getPaymentMethod())
                .paymentStatus(payment.getPaymentStatus())
                .paymentReference(payment.getPaymentReference())
                .transactionId(payment.getTransactionId())
                .createdAt(payment.getCreatedAt())
                .paidAt(payment.getPaidAt())
                .build();
    }

    public PaymentDetailsResponse getRepairPaymentDetails(UUID repairRequestId) {
        RepairPayment payment = repairPaymentRepository.findByRepairRequestId(repairRequestId)
                .orElseThrow(() -> new RuntimeException("Payment not found for repair request: " + repairRequestId));
        return PaymentDetailsResponse.builder()
                .paymentId(payment.getId())
                .repairRequestId(payment.getRepairRequestId())
                .amount(payment.getAmount())
                .paymentMethod(payment.getPaymentMethod())
                .paymentStatus(payment.getPaymentStatus())
                .paymentReference(payment.getPaymentReference())
                .transactionId(payment.getTransactionId())
                .createdAt(payment.getCreatedAt())
                .build();
    }

    @Transactional
    public void processRefund(RefundRequest request) {
        PaymentDetailsResponse paymentDetails = getPaymentDetails(request.getPaymentId());
        BigDecimal refundAmount = request.getRefundAmount() != null ? request.getRefundAmount() : paymentDetails.getAmount();

        if (refundAmount.compareTo(paymentDetails.getAmount()) > 0) {
            throw new IllegalArgumentException("Refund amount cannot exceed original payment amount");
        }

        if (paymentDetails.getPaymentMethod() == PaymentMethod.CREDIT_CARD) {
            if (paymentDetails.getTransactionId() == null) {
                throw new IllegalStateException("No transaction ID available for refund");
            }
            String authToken = paymobService.authenticateAndGetToken();
            paymobService.refundTransaction(authToken, paymentDetails.getTransactionId(), refundAmount);
        }

        boolean isOrderPayment = paymentDetails.getOrderId() != null;
        updatePaymentStatus(request.getPaymentId(), PaymentStatus.REFUNDED, isOrderPayment);

        log.info("Refund processed for payment ID: {} with amount: {}", 
                request.getPaymentId(), refundAmount);
    }

    @Transactional
    public void updatePaymentStatus(UUID paymentId, PaymentStatus newStatus, boolean isOrderPayment) {
        if (isOrderPayment) {
            OrderPayment payment = orderPaymentRepository.findById(paymentId)
                    .orElseThrow(() -> new RuntimeException("Order payment not found"));
            PaymentStatus oldStatus = payment.getPaymentStatus();
            payment.setPaymentStatus(newStatus);
            if (newStatus == PaymentStatus.COMPLETED) {
                payment.setPaidAt(LocalDateTime.now());
            }
            orderPaymentRepository.save(payment);

            paymentEventPublisher.publishPaymentStatusChanged(paymentId, oldStatus, newStatus, payment.getAmount());
        } else {
            RepairPayment payment = repairPaymentRepository.findById(paymentId)
                    .orElseThrow(() -> new RuntimeException("Repair payment not found"));
            PaymentStatus oldStatus = payment.getPaymentStatus();
            payment.setPaymentStatus(newStatus);
            repairPaymentRepository.save(payment);

            paymentEventPublisher.publishPaymentStatusChanged(paymentId, oldStatus, newStatus, payment.getAmount());
        }

        log.info("Payment status updated to {} for payment ID: {}", newStatus, paymentId);
    }
}