package com.techRestore.tech.restore.controller.auth;

import com.techRestore.tech.restore.controller.BaseController;
import com.techRestore.tech.restore.dto.auth.LoginDto;
import com.techRestore.tech.restore.dto.auth.TokenResponse;
import com.techRestore.tech.restore.dto.delivery.DeliveryRegistration;
import com.techRestore.tech.restore.services.auth.DeliveryAuthServices;
import lombok.RequiredArgsConstructor;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth/delivery")
@RequiredArgsConstructor
public class DeliveryAuthController extends BaseController {

    private final DeliveryAuthServices deliveryAuthServices;

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody DeliveryRegistration deliveryRegistration) {
        String deliveryId = deliveryAuthServices.register(deliveryRegistration);
        return createdResponse("Delivery registered successfully with ID: " + deliveryId);
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody LoginDto loginDto) {
        return ResponseEntity.ok(deliveryAuthServices.login(loginDto));
    }

    @PostMapping("/refresh")
    public ResponseEntity<TokenResponse> refreshToken(@RequestBody String refreshToken) {
        return successResponse(deliveryAuthServices.refreshToken(refreshToken));
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(@RequestBody String refreshToken) {
        deliveryAuthServices.logout(refreshToken);
        return successResponse("Logged out successfully");
    }
}