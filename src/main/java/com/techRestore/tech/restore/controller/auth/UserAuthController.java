package com.techRestore.tech.restore.controller.auth;

import com.techRestore.tech.restore.dto.auth.LoginDto;
import com.techRestore.tech.restore.dto.auth.RefreshTokenDto;
import com.techRestore.tech.restore.dto.auth.TokenResponse;
import com.techRestore.tech.restore.dto.auth.UserRegistration;
import com.techRestore.tech.restore.services.auth.AuthServices;
import lombok.RequiredArgsConstructor;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class UserAuthController {
    private final AuthServices authServices;

    @PostMapping("/register")
    public ResponseEntity<String> create(@RequestBody UserRegistration userRegistration) {
            authServices.register(userRegistration);
            return ResponseEntity.ok("please verify your email to complete the registration");
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody LoginDto loginDto) {
        return ResponseEntity.ok(authServices.login(loginDto));
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<TokenResponse> refreshToken(@RequestBody RefreshTokenDto refreshTokenDto) {
            TokenResponse tokens = authServices.refreshToken(refreshTokenDto.refreshToken());
            return ResponseEntity.ok(tokens);
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(@RequestBody RefreshTokenDto refreshTokenDto) {
        authServices.logout(refreshTokenDto.refreshToken());
        return ResponseEntity.ok("Logged out successfully");
    }
}
