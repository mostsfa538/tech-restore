package com.techRestore.tech.restore.common.controller.auth;

import com.techRestore.tech.restore.common.controller.BaseController;
import com.techRestore.tech.restore.common.dto.auth.LoginDto;
import com.techRestore.tech.restore.common.dto.auth.ShopRegistrationRequest;
import com.techRestore.tech.restore.common.dto.auth.TokenResponse;
import com.techRestore.tech.restore.common.dto.auth.UserRegistration;
import com.techRestore.tech.restore.common.dto.common.ForgotPasswordRequest;
import com.techRestore.tech.restore.common.dto.common.ResetPasswordRequest;
import com.techRestore.tech.restore.common.dto.email.EmailVerification;
import com.techRestore.tech.restore.common.dto.email.ResendOpt;
import com.techRestore.tech.restore.common.services.auth.AuthServices;
import com.techRestore.tech.restore.common.services.emailVerification.EmailServices;
import com.techRestore.tech.restore.common.utils.CookieUtil;
import com.techRestore.tech.restore.delivery.dto.DeliveryRegistration;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import java.util.Map;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController extends BaseController {

    private final AuthServices authServices;
    private final EmailServices emailServices;
    private final CookieUtil cookieUtil;

    @PostMapping("/register/user")
    public ResponseEntity<Map<String, String>> registerUser(@Valid @RequestBody UserRegistration userRegistration) {
        authServices.registerUser(userRegistration);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("message", "User registered successfully. Please check your email for verification."));
    }

    @PostMapping("/register/delivery")
    public ResponseEntity<Map<String, String>> registerDelivery(
            @Valid @RequestBody DeliveryRegistration deliveryRegistration) {
        String deliveryId = authServices.registerDelivery(deliveryRegistration);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("message", "Delivery registered successfully", "delivery_id", deliveryId));
    }

    @PostMapping("/register/shop")
    public ResponseEntity<Map<String, String>> registerShop(
            @Valid @RequestBody ShopRegistrationRequest shopRegistrationRequest) {
        String result = authServices.registerShop(shopRegistrationRequest);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("message", result));
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody @Valid LoginDto loginDto, HttpServletRequest request,
            HttpServletResponse response) {
        return ResponseEntity.ok(authServices.login(loginDto, request, response));
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<TokenResponse> refreshToken(HttpServletRequest request, HttpServletResponse response) {
        return successResponse(authServices.refreshToken(request, response));
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(HttpServletRequest request, HttpServletResponse response) {
            authServices.logout(request, response);
            return ResponseEntity.ok(Map.of("message", "Logged out successfully"));
    }

    @PostMapping("/logout-all")
    public ResponseEntity<String> logoutAllSessions(HttpServletRequest request, HttpServletResponse response) {
        try {
            authServices.logoutFromAllDevices(request, response);
            return successResponse("Logged out from all sessions successfully");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Logout failed");
        }
    }

    @GetMapping("/get-code")
    public ResponseEntity<Void> getCode(@RequestBody @Valid EmailVerification emailVerification) {
        emailServices.sendOtpEmail(emailVerification.email());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/verify-email")
    public ResponseEntity<Void> verify(@RequestBody @Valid EmailVerification emailVerification) {
        emailServices.verifyOtp(emailVerification.email(), emailVerification.optCode());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/resend-otp")
    public ResponseEntity<Void> resend(@RequestBody @Valid ResendOpt resendOpt) {
        emailServices.resendOtp(resendOpt.email());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<Void> forgotPassword(@RequestBody @Valid ForgotPasswordRequest request) {
        emailServices.forgotPassword(request.email());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Void> resetPassword(@RequestBody @Valid ResetPasswordRequest request) {
        emailServices.resetPassword(
                request.email(),
                request.otp(),
                request.newPassword(),
                request.confirmPassword());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/test-cookie")
    public ResponseEntity<?> testCookie(HttpServletRequest request) {
        Optional<String> refreshToken = cookieUtil.getRefreshTokenFromCookie(request);
        return ResponseEntity.ok(Map.of(
                "hasCookie", refreshToken.isPresent(),
                "cookieValue", refreshToken.orElse("none")
        ));
    }
}