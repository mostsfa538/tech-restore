package com.techRestore.tech.restore.controller.payment;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.security.core.Authentication;

import com.techRestore.tech.restore.dto.payment.PaymentInitiationDto;
import com.techRestore.tech.restore.exception.ActivationException;
import com.techRestore.tech.restore.exception.NotFoundException;
import com.techRestore.tech.restore.model.entities.User;
import com.techRestore.tech.restore.repository.UserRepository;
import com.techRestore.tech.restore.services.payment.OrderPaymentService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/payments")
public class PaymentController {
    
  private final OrderPaymentService orderPaymentService;
  private final UserRepository userRepository;

  private UUID getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "No authenticated user found");
        }

        String email = authentication.getName();
        User user = userRepository.findByEmail(email);

        if (user == null) {
            throw new NotFoundException("User not found: " + email);
        }
        if (!user.isActivate()) {
            throw new ActivationException("User account is deactivated: " + email);
        }

        return user.getId();
    }

    @PostMapping("/card/{orderId}")
    public ResponseEntity<PaymentInitiationDto> initiateCardPayment(@PathVariable UUID orderId) { 
        UUID userId = getCurrentUserId();
        PaymentInitiationDto paymentDto = orderPaymentService.initiateCardPayment(orderId, userId);
        return ResponseEntity.ok(paymentDto);
    }

    
}
