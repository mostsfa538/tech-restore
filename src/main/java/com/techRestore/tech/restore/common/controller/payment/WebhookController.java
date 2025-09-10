package com.techRestore.tech.restore.common.controller.payment;

import com.techRestore.tech.restore.common.services.payment.ProcessingService;
import com.techRestore.tech.restore.common.controller.BaseController;
import com.techRestore.tech.restore.common.services.payment.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import jakarta.servlet.http.HttpServletRequest;

import java.util.Map;

@RestController
@RequestMapping("/api/webhook")
@RequiredArgsConstructor
public class WebhookController extends BaseController{

    private final @Qualifier("paymentProcessingService") ProcessingService processingService;
    private final PaymentService paymentService;

    @PostMapping("/paymob/callback")
    public ResponseEntity<String> handlePaymobCallback(@RequestBody Map<String, Object> payload, HttpServletRequest request) {
        paymentService.handlePaymentCallback(payload, request);
        return successMessageResponse("Callback received");
    }

    @GetMapping("/paymob/response")
    public ResponseEntity<String> handlePaymobResponse(HttpServletRequest request) {
        String success = request.getParameter("success");
        String orderId = request.getParameter("order");
        String transactionId = request.getParameter("id");
        paymentService.handlePaymentResponse(success, orderId, transactionId);
        return successMessageResponse("Payment successful");
    }
}