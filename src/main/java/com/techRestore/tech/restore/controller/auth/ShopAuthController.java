package com.techRestore.tech.restore.controller.auth;

import com.techRestore.tech.restore.dto.auth.LoginDto;
import com.techRestore.tech.restore.dto.auth.ShopRegistrationRequest;
import com.techRestore.tech.restore.dto.auth.TokenResponse;
import com.techRestore.tech.restore.services.auth.ShopAuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth/shops")
public class ShopAuthController {
    @Autowired
    private ShopAuthService shopAuthService;

    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@RequestBody LoginDto loginDto) {
        try {
            TokenResponse token = shopAuthService.login(loginDto);
            return ResponseEntity.ok().body(token);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody ShopRegistrationRequest shopRegistrationRequest) {
        try {
            String message = shopAuthService.register(shopRegistrationRequest);
            return ResponseEntity.ok().body(message);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
