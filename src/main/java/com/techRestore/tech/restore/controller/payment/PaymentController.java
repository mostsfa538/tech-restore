package com.techRestore.tech.restore.controller.payment;

import com.techRestore.tech.restore.dto.payment.PaymentCallbackRequest;
import com.techRestore.tech.restore.dto.payment.PaymentDetailsResponse;
import com.techRestore.tech.restore.dto.payment.PaymentProcessRequest;
import com.techRestore.tech.restore.dto.payment.PaymentProcessResponse;
import com.techRestore.tech.restore.dto.payment.RefundRequest;
import com.techRestore.tech.restore.services.payment.PaymentCallbackService;
import com.techRestore.tech.restore.services.payment.PaymentService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Slf4j
public class PaymentController {
    
    private final PaymentService paymentService;
    private final PaymentCallbackService paymentCallbackService;

    @PostMapping("/process")
    public ResponseEntity<PaymentProcessResponse> processPayment(
            @Valid @RequestBody PaymentProcessRequest request) {
        try {
            log.info("Processing payment request: orderId={}, amount={}, method={}", 
                request.getOrderId(), request.getAmount(), request.getPaymentMethod());
            PaymentProcessResponse response = paymentService.processPayment(request);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            log.error("Validation error processing payment: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(PaymentProcessResponse.builder()
                            .status(com.techRestore.tech.restore.model.enums.PaymentStatus.FAILED)
                            .message("Validation error: " + e.getMessage())
                            .build());
        } catch (Exception e) {
            log.error("Error processing payment: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(PaymentProcessResponse.builder()
                            .status(com.techRestore.tech.restore.model.enums.PaymentStatus.FAILED)
                            .message("Payment processing failed: " + e.getMessage())
                            .build());
        }
    }

    @PostMapping("/callback")
    public ResponseEntity<String> handlePaymentCallback(
            @RequestBody PaymentCallbackRequest callbackRequest) {
        try {
            log.info("Received payment callback for order: {}", callbackRequest.getOrder_id());
            paymentCallbackService.handlePaymentCallback(callbackRequest);
            return ResponseEntity.ok("Callback processed successfully");
        } catch (Exception e) {
            log.error("Error processing payment callback: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Callback processing failed: " + e.getMessage());
        }
    }

    @GetMapping("/{paymentId}")
    public ResponseEntity<PaymentDetailsResponse> getPaymentDetails(@PathVariable UUID paymentId) {
        try {
            PaymentDetailsResponse response = paymentService.getPaymentDetails(paymentId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error retrieving payment details: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<PaymentDetailsResponse> getOrderPaymentDetails(@PathVariable UUID orderId) {
        try {
            PaymentDetailsResponse response = paymentService.getOrderPaymentDetails(orderId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error retrieving order payment details: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @GetMapping("/repair/{repairRequestId}")
    public ResponseEntity<PaymentDetailsResponse> getRepairPaymentDetails(@PathVariable UUID repairRequestId) {
        try {
            PaymentDetailsResponse response = paymentService.getRepairPaymentDetails(repairRequestId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error retrieving repair payment details: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @PostMapping("/refund")
    public ResponseEntity<String> processRefund(@RequestBody RefundRequest request) {
        try {
            paymentService.processRefund(request);
            return ResponseEntity.ok("Refund processed successfully");
        } catch (Exception e) {
            log.error("Error processing refund: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Refund processing failed: " + e.getMessage());
        }
    }
}