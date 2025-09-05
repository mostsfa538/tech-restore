package com.techRestore.tech.restore.controller.payment;


import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.techRestore.tech.restore.services.payment.OrderPaymentService;


@RestController
@RequestMapping("/api/webhook")
@RequiredArgsConstructor
public class WebhookController {

  private final OrderPaymentService orderPaymentService;

  @PostMapping("/paymob/callback")
    public ResponseEntity<String> handlePaymobCallback(
            @RequestBody Map<String, Object> payload,
            HttpServletRequest request) {
        try {
            orderPaymentService.handlePaymentCallback(payload, request);
            return ResponseEntity.ok("Callback received");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/paymob/response")
    public ResponseEntity<String> handlePaymentResponse(@RequestParam Map<String, String> queryParams) {
        String success = queryParams.get("success");
        String message = "Payment " + ("true".equalsIgnoreCase(success) ? "Successful!" : "Failed.");
        return ResponseEntity.ok(message);
    }
  
  
}
