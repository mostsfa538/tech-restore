package com.techRestore.tech.restore.contoller;

import com.techRestore.tech.restore.dto.LoginDto;
import com.techRestore.tech.restore.dto.UserDto;
import com.techRestore.tech.restore.services.AuthServices;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class UserController {
    private final AuthServices authServices;

    @PostMapping("/create")
    public ResponseEntity<String> create(@RequestBody UserDto userDto) {
        try {
            String id = authServices.register(userDto);
            return ResponseEntity.ok("ok id: " + id);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Email is already Exist");
        }
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody LoginDto loginDto) {
        try {
            authServices.login(loginDto);
            return ResponseEntity.ok().body("login success");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("User not found");
        }
    }
}
