package com.techRestore.tech.restore.shop.controller;

import com.techRestore.tech.restore.common.controller.BaseController;
import com.techRestore.tech.restore.common.dto.payment.PaymentInitiationDto;
import com.techRestore.tech.restore.common.exception.CustomException;
import com.techRestore.tech.restore.common.model.entities.Shop;
import com.techRestore.tech.restore.common.model.enums.PaymentStatus;
import com.techRestore.tech.restore.common.services.payment.PaymentService;
import com.techRestore.tech.restore.shop.dto.subscription.SubscriptionRequestDto;
import com.techRestore.tech.restore.shop.dto.subscription.SubscriptionResponseDto;
import com.techRestore.tech.restore.shop.repository.ShopRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/subscriptions")
// @PreAuthorize("hasRole('SHOP')")
public class SubscriptionController extends BaseController {

    private final PaymentService paymentService;
    private final ShopRepository shopRepository;

    private UUID getCurrentShopId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        return shopRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(HttpStatus.UNAUTHORIZED, "Shop not found"))
                .getId();
    }

    // ***************** i added 2 options ay 7aga ba2a*****************
    /*
     * 1- if shop wants to renew the subscriptoin while his period not ended yet.
     * 2- if shop's subscritpion period ended and he want to renew it.
     * 3- Admin confirm the subscription payment if it's cash only.
     */
    @PostMapping("/card")
    public ResponseEntity<PaymentInitiationDto> initiateCardSubscription(
            @RequestBody SubscriptionRequestDto request) {
        UUID shopId = getCurrentShopId();
        PaymentInitiationDto dto = paymentService.initiateCardSubscriptionPayment(shopId, request);
        return createdResponse(dto);
    }

    @PostMapping("/cash")
    public ResponseEntity<Map<String, String>> initiateCashSubscription(
            @RequestBody SubscriptionRequestDto request) {
        UUID shopId = getCurrentShopId();
        paymentService.initiateCashSubscriptionPayment(shopId, request);
        return ResponseEntity.ok(Map.of("message", "Cash payment initiated. Please pay admin EGP " +
                (1000 * request.getMonths()), "months", request.getMonths().toString()));
    }

    @PostMapping("/renew/card/{shopEmail}")
    public ResponseEntity<PaymentInitiationDto> renewCardSubscription(
            @PathVariable String shopEmail,
            @RequestBody SubscriptionRequestDto request) {

        Shop shop = shopRepository.findByEmail(shopEmail)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "Shop not found"));

        if (!shop.isSubscriptionActive()) {
            shop.setActivate(true);
        }

        return ResponseEntity.ok(paymentService.initiateCardSubscriptionPayment(shop.getId(), request));
    }

    @PostMapping("/renew/cash/{shopEmail}")
    public ResponseEntity<Map<String, String>> renewCashSubscription(
            @PathVariable String shopEmail,
            @RequestBody SubscriptionRequestDto request) {

        Shop shop = shopRepository.findByEmail(shopEmail)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "Shop not found"));

        paymentService.initiateCashSubscriptionPayment(shop.getId(), request);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Cash payment initiated! Contact to arrange 1  to 1 meetting " +
                (request.getMonths() * 1000) + " EGP");
        response.put("status", "PENDING");
        response.put("paymentReference", "CASH-SUB-" + shopEmail);

        return ResponseEntity.ok(response);
    }

    // ***************get Current subscription ***************************** */
    @GetMapping
    public ResponseEntity<SubscriptionResponseDto> getMySubscription() {
        UUID shopId = getCurrentShopId();
        SubscriptionResponseDto dto = paymentService.getShopSubscription(shopId);
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/all")
    public ResponseEntity<List<SubscriptionResponseDto>> getAllSubscription() {
        UUID shopId = getCurrentShopId();
        List<SubscriptionResponseDto> dtos = paymentService.getShopAllSubscription(shopId);
        return ResponseEntity.ok(dtos);
    }

    @PostMapping("/cash/confirm/{paymentId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> confirmCashSubscription(
            @PathVariable UUID paymentId, @RequestParam PaymentStatus status) {
        paymentService.updateCashPaymentStatus(paymentId, status);
        return ResponseEntity.ok(Map.of("message", "Subscription payment confirmed"));
    }

    @GetMapping("/renew/status/{shopEmail}")
    @PreAuthorize("hasRole('')")
    public ResponseEntity<SubscriptionResponseDto> getSubscriptionStatus(@PathVariable String shopEmail) {
        Shop shop = shopRepository.findByEmail(shopEmail)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "Shop not found"));
        SubscriptionResponseDto dto = paymentService.getShopSubscription(shop.getId());
        return ResponseEntity.ok(dto);
    }
}
