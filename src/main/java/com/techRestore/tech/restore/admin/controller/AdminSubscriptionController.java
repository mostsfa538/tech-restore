package com.techRestore.tech.restore.admin.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.techRestore.tech.restore.admin.dto.SubscriptionDetails;
import com.techRestore.tech.restore.admin.service.AdminSubscriptionService;
import com.techRestore.tech.restore.common.dto.payment.PaymentDto;
import com.techRestore.tech.restore.common.model.entities.Payment;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/admin/subscriptions")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminSubscriptionController {

    private final AdminSubscriptionService adminSubscriptionService;

    @PostMapping("/cash/confirm/{paymentId}")
    public ResponseEntity<String> confirmCashPayment(@PathVariable UUID paymentId) {
        String message = adminSubscriptionService.confirmSubscriptionCashPayment(paymentId);
        return ResponseEntity.ok(message);
    }

    @PostMapping("/cash/reject/{paymentId}")
    public ResponseEntity<String> rejectCashPayment(@PathVariable UUID paymentId) {
        String message = adminSubscriptionService.RejectSubscriptionCashPayment(paymentId);
        return ResponseEntity.ok(message);
    }

    @GetMapping("/subscriptions-with-payment")
    public ResponseEntity<Page<SubscriptionDetails>> getAllSubscriptionsWithPayment(Pageable pageable) {
        Page<SubscriptionDetails> page = adminSubscriptionService.getAllSubscriptionsWithPayment(pageable);
        return ResponseEntity.ok(page);
    }

    @GetMapping("/shop/{shopId}")
    public ResponseEntity<List<SubscriptionDetails>> getSubscriptionsByShop(@PathVariable UUID shopId) {
        List<SubscriptionDetails> subscriptions = adminSubscriptionService.getSubscriptionsByShop(shopId);
        return ResponseEntity.ok(subscriptions);
    }

    @GetMapping("/{subscriptionId}")
    public ResponseEntity<SubscriptionDetails> getSubscription(@PathVariable UUID subscriptionId) {
        SubscriptionDetails subscription = adminSubscriptionService.getSubscription(subscriptionId);
        return ResponseEntity.ok(subscription);
    }


    @GetMapping("/cash/pending")
    public ResponseEntity<Page<PaymentDto>> getPendingCashPayments(Pageable pageable) {
        Page<PaymentDto> page = adminSubscriptionService.getPendingCashPayments(pageable);
        return ResponseEntity.ok(page);
    }


}
