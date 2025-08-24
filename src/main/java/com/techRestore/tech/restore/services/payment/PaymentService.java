package com.techRestore.tech.restore.services.payment;

import com.techRestore.tech.restore.dto.payment.PaymentResponseDTO;
import com.techRestore.tech.restore.dto.payment.ProcessPaymentRequestDTO;
import com.techRestore.tech.restore.dto.payment.RefundRequestDTO;
import com.techRestore.tech.restore.dto.payment.UserPaymentMethodRequestDTO;
import com.techRestore.tech.restore.dto.payment.UserPaymentMethodResponseDTO;
import com.techRestore.tech.restore.exception.NotFoundException;
import com.techRestore.tech.restore.model.entities.Order;
import com.techRestore.tech.restore.model.entities.Payment;
import com.techRestore.tech.restore.model.enums.OrderStatus;
import com.techRestore.tech.restore.model.enums.PaymentMethod;
import com.techRestore.tech.restore.model.enums.PaymentStatus;
import com.techRestore.tech.restore.repository.OrderRepository;
import com.techRestore.tech.restore.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;

    @Transactional
    public PaymentResponseDTO processPayment(UUID userId, ProcessPaymentRequestDTO request) {
        Order order = orderRepository.findByIdAndUserId(request.getOrderId(), userId)
                .orElseThrow(() -> new NotFoundException("Order not found"));

        if (order.getStatus() != OrderStatus.PENDING) {
            throw new RuntimeException("Order not in payable state");
        }

        Payment payment = new Payment();
        payment.setUserId(userId);
        payment.setAmount(order.getTotalPrice());
        payment.setType("ORDER_PAYMENT");

        PaymentMethod selectedMethod;
        String paymentDetails = null;
        String processingDetails;

        // Choose payment method
        if (request.getPaymentMethodId() != null) {
            Payment savedMethod = paymentRepository.findById(request.getPaymentMethodId())
                    .orElseThrow(() -> new NotFoundException("Payment method not found"));
            if (!savedMethod.getUserId().equals(userId) || !"SAVED_METHOD".equals(savedMethod.getType())) {
                throw new RuntimeException("Unauthorized");
            }
            selectedMethod = savedMethod.getPaymentMethod();
            paymentDetails = savedMethod.getDetails();
        } else if (request.getPaymentMethod() != null) {
            selectedMethod = request.getPaymentMethod();
        } else {
            throw new RuntimeException("No payment method provided");
        }

        payment.setPaymentMethod(selectedMethod);

        // Process by method
        switch (selectedMethod) {
            case CASH:
                payment.setPaymentStatus(PaymentStatus.PENDING);
                order.setStatus(OrderStatus.PROCESSING);
                processingDetails = "Cash payment pending until order delivery for order: " + order.getId();
                break;

            case CREDIT_CARD:
            case DEBIT_CARD:
                processingDetails = paymentDetails != null
                        ? String.format("%s payment processed with details ending: %s",
                        selectedMethod, maskDetails(paymentDetails))
                        : String.format("%s payment processed for order: %s", selectedMethod, order.getId());
                payment.setPaymentStatus(PaymentStatus.COMPLETED);
                payment.setPaidAt(LocalDateTime.now());
                order.setStatus(OrderStatus.CONFIRMED);
                break;

            case MOBILE_WALLET:
                processingDetails = "Mobile wallet payment processed for order: " + order.getId();
                payment.setPaymentStatus(PaymentStatus.COMPLETED);
                payment.setPaidAt(LocalDateTime.now());
                order.setStatus(OrderStatus.CONFIRMED);
                break;

            case BANK_TRANSFER:
                payment.setPaymentStatus(PaymentStatus.PENDING);
                order.setStatus(OrderStatus.PENDING);
                processingDetails = "Bank transfer initiated for order: " + order.getId() + ", awaiting confirmation";
                break;

            default:
                throw new RuntimeException("Unsupported payment method: " + selectedMethod);
        }

        paymentRepository.save(payment);
        orderRepository.save(order);

        return mapToPaymentResponseDTO(payment, order.getId(), processingDetails);
    }


    @Transactional(readOnly = true)
    public PaymentResponseDTO getPaymentDetails(UUID userId, UUID paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new NotFoundException("Payment not found"));

        if (!payment.getUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized");
        }

        return mapToPaymentResponseDTO(payment, null, null);
    }

    @Transactional
    public void processRefund(UUID userId, RefundRequestDTO request) {
        Payment payment = paymentRepository.findById(request.getPaymentId())
                .orElseThrow(() -> new NotFoundException("Payment not found"));

        if (!payment.getUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized");
        }
        if (payment.getPaymentStatus() != PaymentStatus.COMPLETED) {
            throw new RuntimeException("Payment not refundable");
        }

        String processingDetails;
        switch (payment.getPaymentMethod()) {
            case CASH:
                processingDetails = "Cash refund processed for payment: " + payment.getId();
                break;
            case CREDIT_CARD:
            case DEBIT_CARD:
            case MOBILE_WALLET:
                processingDetails = String.format("%s refund processed for payment: %s",
                        payment.getPaymentMethod(), payment.getId());
                break;
            case BANK_TRANSFER:
                processingDetails = "Bank transfer refund initiated for payment: " + payment.getId();
                break;
            default:
                throw new RuntimeException("Unsupported refund method: " + payment.getPaymentMethod());
        }

        payment.setPaymentStatus(PaymentStatus.REFUNDED);
        payment.setType("REFUND");
        paymentRepository.save(payment);

        // Optional: update order status if linked
        orderRepository.findById(payment.getId()).ifPresent(order -> {
            order.setStatus(OrderStatus.CANCELLED);
            orderRepository.save(order);
        });
    }

 
    @Transactional(readOnly = true)
    public List<UserPaymentMethodResponseDTO> getSavedPaymentMethods(UUID userId) {
        List<Payment> methods = paymentRepository.findByUserIdAndType(userId, "SAVED_METHOD");
        return methods.stream().map(this::mapToUserPaymentMethodResponseDTO).collect(Collectors.toList());
    }


    @Transactional
    public UserPaymentMethodResponseDTO addPaymentMethod(UUID userId, UserPaymentMethodRequestDTO request) {
        Payment method = new Payment();
        method.setUserId(userId);
        method.setPaymentMethod(request.getPaymentMethod());
        method.setDetails(request.getDetails());
        method.setDefault(request.isDefault());
        method.setType("SAVED_METHOD");
        method.setPaymentStatus(PaymentStatus.PENDING);
        paymentRepository.save(method);
        return mapToUserPaymentMethodResponseDTO(method);
    }

  
    @Transactional
    public void removePaymentMethod(UUID userId, UUID methodId) {
        Payment method = paymentRepository.findById(methodId)
                .orElseThrow(() -> new NotFoundException("Payment method not found"));
        if (!method.getUserId().equals(userId) || !"SAVED_METHOD".equals(method.getType())) {
            throw new RuntimeException("Unauthorized");
        }
        paymentRepository.delete(method);
    }



    private PaymentResponseDTO mapToPaymentResponseDTO(Payment payment, UUID orderId, String processingDetails) {
        PaymentResponseDTO dto = new PaymentResponseDTO();
        dto.setId(payment.getId());
        dto.setAmount(payment.getAmount());
        dto.setPaymentMethod(payment.getPaymentMethod());
        dto.setPaymentStatus(payment.getPaymentStatus());
        dto.setPaymentReference(payment.getPaymentReference());
        dto.setCreatedAt(payment.getCreatedAt());
        dto.setPaidAt(payment.getPaidAt());
        dto.setOrderId(orderId);
        dto.setProcessingDetails(processingDetails);
        return dto;
    }

    private UserPaymentMethodResponseDTO mapToUserPaymentMethodResponseDTO(Payment method) {
        UserPaymentMethodResponseDTO dto = new UserPaymentMethodResponseDTO();
        dto.setId(method.getId());
        dto.setPaymentMethod(method.getPaymentMethod());
        dto.setDetails(maskDetails(method.getDetails()));
        dto.setDefault(method.isDefault());
        dto.setCreatedAt(method.getCreatedAt());
        return dto;
    }

    private String maskDetails(String details) {
        if (details == null || details.length() < 4) return "****";
        return "****" + details.substring(details.length() - 4);
    }
}
