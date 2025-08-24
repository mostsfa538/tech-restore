package com.techRestore.tech.restore.controller.payment;

import com.techRestore.tech.restore.dto.payment.PaymentResponseDTO;
import com.techRestore.tech.restore.dto.payment.ProcessPaymentRequestDTO;
import com.techRestore.tech.restore.dto.payment.RefundRequestDTO;
import com.techRestore.tech.restore.dto.payment.UserPaymentMethodRequestDTO;
import com.techRestore.tech.restore.dto.payment.UserPaymentMethodResponseDTO;
import com.techRestore.tech.restore.model.entities.User;
import com.techRestore.tech.restore.repository.UserRepository;
import com.techRestore.tech.restore.services.payment.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;
    private final UserRepository userRepository;

    private UUID getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("No authenticated user found");
        }

        String email = authentication.getName();
        User user = userRepository.findByEmail(email);

        if (user == null || !user.isActivate()) {
            throw new RuntimeException("User account is deactivated or not found: " + email);
        }

        return user.getId();
}


    @PostMapping("/process")
    public ResponseEntity<PaymentResponseDTO> processPayment(@RequestBody ProcessPaymentRequestDTO request) {
        UUID userId = getCurrentUserId();
        PaymentResponseDTO response = paymentService.processPayment(userId, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{paymentId}")
    public ResponseEntity<PaymentResponseDTO> getPaymentDetails(@PathVariable UUID paymentId) {
        UUID userId = getCurrentUserId();
        PaymentResponseDTO response = paymentService.getPaymentDetails(userId, paymentId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/refund")
    public ResponseEntity<Void> processRefund(@RequestBody RefundRequestDTO request) {
        UUID userId = getCurrentUserId();
        paymentService.processRefund(userId, request);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/methods")
    public ResponseEntity<List<UserPaymentMethodResponseDTO>> getSavedPaymentMethods() {
        UUID userId = getCurrentUserId();
        List<UserPaymentMethodResponseDTO> methods = paymentService.getSavedPaymentMethods(userId);
        return ResponseEntity.ok(methods);
    }

    @PostMapping("/methods")
    public ResponseEntity<UserPaymentMethodResponseDTO> addPaymentMethod(@RequestBody UserPaymentMethodRequestDTO request) {
        UUID userId = getCurrentUserId();
        UserPaymentMethodResponseDTO response = paymentService.addPaymentMethod(userId, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/methods/{methodId}")
    public ResponseEntity<Void> removePaymentMethod(@PathVariable UUID methodId) {
        UUID userId = getCurrentUserId();
        paymentService.removePaymentMethod(userId, methodId);
        return ResponseEntity.noContent().build();
    }
}
