package com.techRestore.tech.restore.services.auth;

import com.techRestore.tech.restore.dto.auth.LoginDto;
import com.techRestore.tech.restore.dto.auth.TokenResponse;
import com.techRestore.tech.restore.dto.delivery.DeliveryRegistration;
import com.techRestore.tech.restore.exception.EmailAlreadyExistsException;
import com.techRestore.tech.restore.exception.IllegalArgumentException;
import com.techRestore.tech.restore.exception.NotFoundException;
import com.techRestore.tech.restore.model.enums.Role;
import com.techRestore.tech.restore.security.config.CustomAuthenticationManager;
import com.techRestore.tech.restore.security.jwt.JwtService;
import com.techRestore.tech.restore.security.jwt.RefreshTokenService;
import com.techRestore.tech.restore.model.entities.Delivery;
import com.techRestore.tech.restore.repository.DeliveryRepository;
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
public class DeliveryAuthServices {
    private final DeliveryRepository deliveryRepository;
    private final PasswordEncoder passwordEncoder;
    private final CustomAuthenticationManager customAuthenticationManager;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;

    public String register(DeliveryRegistration deliveryRegistration) {
        try {
            Delivery delivery = new Delivery();
            delivery.setEmail(deliveryRegistration.getEmail());
            delivery.setPassword(passwordEncoder.encode(deliveryRegistration.getPassword()));
            delivery.setName(deliveryRegistration.getName());
            delivery.setAddress(deliveryRegistration.getAddress());
            delivery.setPhone(deliveryRegistration.getPhone());
            delivery.setRole(Role.DELIVERY);
            delivery.setCreatedAt(LocalDateTime.now());

            Delivery savedDelivery = deliveryRepository.save(delivery);
            return savedDelivery.getId().toString();
        } catch (Exception e) {
            System.err.println("Error saving delivery: " + e.getMessage());
            throw new IllegalArgumentException("Failed to create delivery: " + e.getMessage());
        }
    }

    public TokenResponse login(LoginDto loginDto) {
        try {
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
            throw new IllegalArgumentException("Invalid Delivery");
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

            Delivery delivery = deliveryRepository.findByEmail(username)
                    .orElseThrow(() -> new NotFoundException("Delivery not found"));

            Authentication authentication = new UsernamePasswordAuthenticationToken(
                    username, null,
                    Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + delivery.getRole().name())));

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
        } catch (Exception ignore) {
        }
    }
}