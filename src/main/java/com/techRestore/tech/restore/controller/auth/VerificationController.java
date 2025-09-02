package com.techRestore.tech.restore.controller.auth;

import com.techRestore.tech.restore.dto.common.ResetPasswordRequest;
import com.techRestore.tech.restore.dto.common.email.EmailVerification;
import com.techRestore.tech.restore.dto.common.email.ResendOpt;
import com.techRestore.tech.restore.services.emailVerification.EmailServices;
import com.techRestore.tech.restore.dto.common.ForgotPasswordRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class VerificationController {
    private final EmailServices emailServices;

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
                request.confirmPassword()
        );
        return ResponseEntity.ok().build();
    }

}
