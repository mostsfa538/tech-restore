package com.techRestore.tech.restore.common.controller.auth;

import com.techRestore.tech.restore.common.dto.auth.LoginDto;
import com.techRestore.tech.restore.common.dto.auth.ShopRegistrationRequest;
import com.techRestore.tech.restore.common.services.auth.ShopAuthService;

import jakarta.validation.Valid;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth/shops")
public class ShopAuthController {
    @Autowired
    private ShopAuthService shopAuthService;

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody @Valid LoginDto loginDto) {
        try {
            Map<String, Object> response = shopAuthService.login(loginDto);
            return ResponseEntity.ok().body(response);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(e.getMessage());
        } catch (Exception e) {
            throw new RuntimeException("Login failed: " + e.getMessage());
        }
    }

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody @Valid ShopRegistrationRequest shopRegistrationRequest) {
        try {
            String message = shopAuthService.register(shopRegistrationRequest);
            return ResponseEntity.ok().body(message);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
