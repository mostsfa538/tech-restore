package com.techRestore.tech.restore.controller.auth;

import com.techRestore.tech.restore.dto.auth.LoginDto;
import com.techRestore.tech.restore.dto.auth.RefreshTokenDto;
import com.techRestore.tech.restore.dto.auth.TokenResponse;
import com.techRestore.tech.restore.dto.auth.UserRegistration;
import com.techRestore.tech.restore.services.auth.AuthServices;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class UserAuthController {
    private final AuthServices authServices;

    @PostMapping("/create")
    public ResponseEntity<String> create(@RequestBody UserRegistration userRegistration) {
        try {
            String id = authServices.register(userRegistration);
            return ResponseEntity.ok("User created successfully with ID: " + id);
        } catch (RuntimeException e) {
            if (e.getMessage().contains("Email already exists")) {
                return ResponseEntity.badRequest().body("Email already exists");
            }
            return ResponseEntity.badRequest().body("Registration failed: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Unexpected error during registration: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("An unexpected error occurred");
        }
    }

    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@RequestBody LoginDto loginDto) {
        try {
            TokenResponse tokens = authServices.login(loginDto);
            return ResponseEntity.ok(tokens);
        } catch (RuntimeException e) {
            System.out.println(e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
    
    @PostMapping("/refresh-token")
    public ResponseEntity<TokenResponse> refreshToken(@RequestBody RefreshTokenDto refreshTokenDto) {
        try {
            TokenResponse tokens = authServices.refreshToken(refreshTokenDto.refreshToken());
            return ResponseEntity.ok(tokens);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(@RequestBody RefreshTokenDto refreshTokenDto) {
        authServices.logout(refreshTokenDto.refreshToken());
        return ResponseEntity.ok("Logged out successfully");
    }

}
