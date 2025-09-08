package com.techRestore.tech.restore.common.services.auth;

import com.techRestore.tech.restore.common.dto.auth.LoginDto;
import com.techRestore.tech.restore.common.dto.auth.TokenResponse;
import com.techRestore.tech.restore.common.dto.auth.UserRegistration;
import com.techRestore.tech.restore.common.exception.NotFoundException;
import com.techRestore.tech.restore.common.model.entities.User;
import com.techRestore.tech.restore.common.model.enums.Role;
import com.techRestore.tech.restore.common.security.config.CustomAuthenticationManager;
import com.techRestore.tech.restore.common.security.jwt.JwtService;
import com.techRestore.tech.restore.common.security.jwt.RefreshTokenService;
import com.techRestore.tech.restore.common.services.emailVerification.EmailServices;
import com.techRestore.tech.restore.common.utils.EmailValidatorService;
import com.techRestore.tech.restore.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthServices {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final CustomAuthenticationManager customAuthenticationManager;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final EmailServices emailService;
    private final EmailValidatorService emailValidatorService;

    public void register(UserRegistration userRegistration) {
        emailValidatorService.validateUniqueEmail(userRegistration.email());
        try {
            User user = new User();
            user.setFirst_name(userRegistration.first_name());
            user.setLast_name(userRegistration.last_name());
            user.setEmail(userRegistration.email());
            user.setRole(Role.GUEST);
            user.setPassword(passwordEncoder.encode(userRegistration.password()));
            user.setPhone(userRegistration.phone());
            user.setCreatedAt(LocalDateTime.now());

            userRepository.save(user);
            emailService.generateAndSendOtp(user.getEmail());

        } catch (Exception e) {
            System.err.println("Error saving user: " + e.getMessage());
            throw new IllegalArgumentException("Failed to create user: " + e.getMessage());
        }
    }

    public Map<String, Object> login(LoginDto loginDto) {
        try {
            Authentication authentication = customAuthenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginDto.email(), loginDto.password()));

            String accessToken = jwtService.generateAccessToken(authentication);
            String refreshToken = jwtService.generateRefreshToken(authentication);

            refreshTokenService.saveRefreshToken(authentication.getName(), refreshToken);

            User user = userRepository.findByEmail(authentication.getName());
            if (user == null) {
                throw new IllegalArgumentException("User not found");
            }

            // Create a map to hold the response
            Map<String, Object> response = new HashMap<>();
            response.put("access_token", accessToken);
            response.put("refresh_token", refreshToken);
            response.put("token_type", "Bearer");
            response.put("expires_in", 60 * 60);
            response.put("user_id", user.getId()); // Include the user's UUID

            return response;
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid User");
        }
    }

    public TokenResponse refreshToken(String refreshToken) {
        try {
            if (jwtService.isTokenExpired(refreshToken) || !jwtService.isRefreshToken(refreshToken)) {
                throw new IllegalArgumentException("Invalid refresh token");
            }

            String username = jwtService.extractClaim(refreshToken, claims -> claims.get("username", String.class));

            if (!refreshTokenService.isValidRefreshToken(username, refreshToken)) {
                throw new IllegalArgumentException("Invalid refresh token");
            }

            User user = userRepository.findByEmail(username);
            if (user == null) {
                throw new NotFoundException("User not found");
            }

            Authentication authentication = new UsernamePasswordAuthenticationToken(
                    username, null,
                    Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + user.getRole().name())));

            String newAccessToken = jwtService.generateAccessToken(authentication);
            String newRefreshToken = jwtService.generateRefreshToken(authentication);

            refreshTokenService.saveRefreshToken(username, newRefreshToken);

            return new TokenResponse(
                    newAccessToken,
                    newRefreshToken,
                    "Bearer",
                    60 * 60 // sa3a
            );
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid refresh token");
        }
    }

    public void logout(String refreshToken) {
        try {
            String username = jwtService.extractClaim(refreshToken, claims -> claims.get("username", String.class));
            refreshTokenService.deleteRefreshToken(username);
        } catch (Exception ignored) {
        }
    }
}