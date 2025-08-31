package com.techRestore.tech.restore.services.auth;

import com.techRestore.tech.restore.dto.auth.LoginDto;
import com.techRestore.tech.restore.dto.auth.TokenResponse;
import com.techRestore.tech.restore.dto.auth.UserRegistration;
import com.techRestore.tech.restore.model.enums.Role;
import com.techRestore.tech.restore.security.config.CustomAuthenticationManager;
import com.techRestore.tech.restore.security.jwt.JwtService;
import com.techRestore.tech.restore.security.jwt.RefreshTokenService;
import com.techRestore.tech.restore.services.EmailService;
import com.techRestore.tech.restore.utils.EmailVerificationUtils;
import com.techRestore.tech.restore.model.entities.User;
import com.techRestore.tech.restore.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;

@Service
@RequiredArgsConstructor
public class AuthServices {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final CustomAuthenticationManager customAuthenticationManager;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final EmailService emailService;

    public String register(UserRegistration userRegistration) {
        if (userRegistration == null) {
            throw new RuntimeException("User data cannot be null");
        }
        if (userRegistration.email() == null || userRegistration.email().trim().isEmpty()) {
            throw new RuntimeException("Email cannot be empty");
        }
        if (userRegistration.password() == null || userRegistration.password().trim().isEmpty()) {
            throw new RuntimeException("Password cannot be empty");
        }
        if (userRegistration.first_name() == null || userRegistration.first_name().trim().isEmpty()) {
            throw new RuntimeException("Name cannot be empty");
        }

        if (userRepository.existsByEmail(userRegistration.email())) {
            throw new RuntimeException("Email already exists");
        }

        try {
            User user = new User();
            user.setFirst_name(userRegistration.first_name());
            user.setLast_name(userRegistration.last_name());
            user.setEmail(userRegistration.email());
            user.setRole(Role.GUEST);
            user.setPassword(passwordEncoder.encode(userRegistration.password()));
            user.setPhone(userRegistration.phone());
            user.setCreatedAt(LocalDateTime.now());
            
            // Set email verification fields
            String verificationToken = EmailVerificationUtils.generateVerificationToken();
            user.setEmailVerificationToken(verificationToken);
            user.setEmailTokenExpiry(EmailVerificationUtils.getTokenExpiry());
            user.setEmailVerified(false);

            User savedUser = userRepository.save(user);
            
            // Send verification email
            try {
                emailService.sendVerificationEmail(user.getEmail(), verificationToken, "users");
            } catch (Exception emailException) {
                System.err.println("Failed to send verification email: " + emailException.getMessage());
                // Continue registration even if email fails
            }
            
            return "User registered successfully. Please check your email to verify your account.";
        } catch (Exception e) {
            System.err.println("Error saving user: " + e.getMessage());
            throw new RuntimeException("Failed to create user: " + e.getMessage());
        }
    }

    public TokenResponse login(LoginDto loginDto) {
        try {
            // Check if user exists and email is verified
            User user = userRepository.findByEmail(loginDto.email());
            if (user != null && !user.isEmailVerified()) {
                throw new RuntimeException("Please verify your email before logging in. Check your inbox for the verification link.");
            }

            Authentication authentication = customAuthenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginDto.email(), loginDto.password()));

            String accessToken = jwtService.generateAccessToken(authentication);
            String refreshToken = jwtService.generateRefreshToken(authentication);

            refreshTokenService.saveRefreshToken(authentication.getName(), refreshToken);

            return new TokenResponse(
                    accessToken,
                    refreshToken,
                    "Bearer",
                    60 * 60);
        } catch (Exception e) {
            throw new RuntimeException("Invalid User");
        }
    }

    public TokenResponse refreshToken(String refreshToken) {
        try {
            if (jwtService.isTokenExpired(refreshToken) || !jwtService.isRefreshToken(refreshToken)) {
                throw new RuntimeException("Invalid refresh token");
            }

            String username = jwtService.extractClaim(refreshToken, claims -> claims.get("username", String.class));

            if (!refreshTokenService.isValidRefreshToken(username, refreshToken)) {
                throw new RuntimeException("Invalid refresh token");
            }

            User user = userRepository.findByEmail(username);
            if (user == null) {
                throw new RuntimeException("User not found");
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
            throw new RuntimeException("Invalid refresh token");
        }
    }

    public void logout(String refreshToken) {
        try {
            String username = jwtService.extractClaim(refreshToken, claims -> claims.get("username", String.class));
            refreshTokenService.deleteRefreshToken(username);
        } catch (Exception e) {
        }
    }

    public String verifyEmail(String token) {
        User user = userRepository.findByEmailVerificationToken(token);
        if (user == null) {
            throw new RuntimeException("Invalid verification token");
        }

        if (EmailVerificationUtils.isTokenExpired(user.getEmailTokenExpiry())) {
            throw new RuntimeException("Verification token has expired");
        }

        user.setEmailVerified(true);
        user.setEmailVerificationToken(null);
        user.setEmailTokenExpiry(null);
        userRepository.save(user);

        // Send welcome email
        try {
            emailService.sendWelcomeEmail(user.getEmail(), user.getFirst_name());
        } catch (Exception e) {
            System.err.println("Failed to send welcome email: " + e.getMessage());
        }

        return "Email verified successfully! You can now log in.";
    }

    public String resendVerificationEmail(String email) {
        User user = userRepository.findByEmail(email);
        if (user == null) {
            throw new RuntimeException("User not found");
        }

        if (user.isEmailVerified()) {
            throw new RuntimeException("Email is already verified");
        }

        // Generate new verification token
        String verificationToken = EmailVerificationUtils.generateVerificationToken();
        user.setEmailVerificationToken(verificationToken);
        user.setEmailTokenExpiry(EmailVerificationUtils.getTokenExpiry());
        userRepository.save(user);

        // Send verification email
        emailService.sendVerificationEmail(user.getEmail(), verificationToken, "users");

        return "Verification email sent successfully";
    }
}