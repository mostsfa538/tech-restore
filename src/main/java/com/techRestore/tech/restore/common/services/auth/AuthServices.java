package com.techRestore.tech.restore.common.services.auth;

import com.techRestore.tech.restore.common.dto.auth.LoginDto;
import com.techRestore.tech.restore.common.dto.auth.ShopRegistrationRequest;
import com.techRestore.tech.restore.common.dto.auth.TokenResponse;
import com.techRestore.tech.restore.common.dto.auth.UserRegistration;
import com.techRestore.tech.restore.common.exception.CustomException;
import com.techRestore.tech.restore.common.exception.NotFoundException;
import com.techRestore.tech.restore.common.model.entities.Delivery;
import com.techRestore.tech.restore.common.model.entities.Shop;
import com.techRestore.tech.restore.common.model.entities.User;
import com.techRestore.tech.restore.common.security.config.CustomAuthenticationManager;
import com.techRestore.tech.restore.common.security.jwt.JwtService;
import com.techRestore.tech.restore.common.security.jwt.RefreshTokenService;
import com.techRestore.tech.restore.common.services.EntityFinderService;
import com.techRestore.tech.restore.common.utils.CookieUtil;
import com.techRestore.tech.restore.delivery.dto.DeliveryRegistration;
import com.techRestore.tech.restore.delivery.service.DeliveryRegistrationStrategy;
import com.techRestore.tech.restore.shop.service.ShopRegistrationStrategy;
import com.techRestore.tech.restore.user.service.user.UserRegistrationStrategy;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthServices {
    private final CustomAuthenticationManager customAuthenticationManager;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final CookieUtil cookieUtil;
    private final EntityFinderService entityFinderService;
    private final UnifiedRegistrationService unifiedRegistrationService;
    private final UserRegistrationStrategy userRegistrationStrategy;
    private final DeliveryRegistrationStrategy deliveryRegistrationStrategy;
    private final ShopRegistrationStrategy shopRegistrationStrategy;

    public void registerUser(UserRegistration userRegistration) {
        unifiedRegistrationService.register(userRegistration, userRegistrationStrategy);
    }

    public String registerDelivery(DeliveryRegistration deliveryRegistration) {
        return unifiedRegistrationService.register(deliveryRegistration, deliveryRegistrationStrategy);
    }

    public String registerShop(ShopRegistrationRequest shopRegistrationRequest) {
        return unifiedRegistrationService.register(shopRegistrationRequest, shopRegistrationStrategy);
    }

    public Map<String, Object> login(LoginDto loginDto, HttpServletResponse response) {
        try {
            Authentication authentication = customAuthenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginDto.email(), loginDto.password()));

            String accessToken = jwtService.generateAccessToken(authentication);
            String refreshToken = jwtService.generateRefreshToken(authentication);

            refreshTokenService.saveRefreshToken(authentication.getName(), refreshToken);

            cookieUtil.addRefreshTokenCookie(response, refreshToken);

            Object principal = authentication.getPrincipal();

            Map<String, Object> responseData = new HashMap<>();
            responseData.put("access_token", accessToken);
            responseData.put("token_type", "Bearer");
            responseData.put("expires_in", 60 * 60);

            if (principal instanceof User user) {
                responseData.put("user_id", user.getId());
                responseData.put("role", user.getRole().name());
            } else if (principal instanceof Shop shop) {
                responseData.put("shop_id", shop.getId());
                responseData.put("role", "SHOP");
            } else if (principal instanceof Delivery delivery) {
                responseData.put("delivery_id", delivery.getId());
                responseData.put("role", "DELIVERY");
            }

            return responseData;
        } catch (Exception e) {
            System.out.println(e.getMessage());
            throw new IllegalArgumentException("Invalid User");
        }
    }

    public TokenResponse refreshToken(HttpServletRequest request, HttpServletResponse response) {
        try {
            Optional<String> refreshTokenOpt = cookieUtil.getRefreshTokenFromCookie(request);

            if (refreshTokenOpt.isEmpty()) {
                throw new IllegalArgumentException("Refresh token not found in cookie");
            }

            String refreshToken = refreshTokenOpt.get();

            if (jwtService.isTokenExpired(refreshToken) || !jwtService.isRefreshToken(refreshToken)) {
                cookieUtil.deleteRefreshTokenCookie(response);
                throw new CustomException(HttpStatus.UNAUTHORIZED, "Invalid refresh token");
            }

            String username = jwtService.extractClaim(refreshToken, claims -> claims.get("username", String.class));

            if (!refreshTokenService.isValidRefreshToken(username, refreshToken)) {
                System.out.println(response);
                cookieUtil.deleteRefreshTokenCookie(response);
                throw new IllegalArgumentException("Invalid refresh token");
            }

            Optional<Authentication> authenticationOpt = entityFinderService
                    .findEntityAndCreateAuthentication(username);

            if (authenticationOpt.isEmpty()) {
                cookieUtil.deleteRefreshTokenCookie(response);
                throw new NotFoundException("Entity not found for email: " + username);
            }

            Authentication authentication = authenticationOpt.get();

            String newAccessToken = jwtService.generateAccessToken(authentication);
            String newRefreshToken = jwtService.generateRefreshToken(authentication);

            refreshTokenService.saveRefreshToken(username, newRefreshToken);
            cookieUtil.addRefreshTokenCookie(response, newRefreshToken);

            return new TokenResponse(
                    newAccessToken,
                    "Bearer",
                    60 * 60);

        } catch (Exception e) {
            cookieUtil.deleteRefreshTokenCookie(response);
            throw new IllegalArgumentException("Invalid refresh token");
        }
    }

    public void logout(HttpServletRequest request, HttpServletResponse response) {
        try {
            Optional<String> refreshTokenOpt = cookieUtil.getRefreshTokenFromCookie(request);

            if (refreshTokenOpt.isPresent()) {
                String refreshToken = refreshTokenOpt.get();
                String username = jwtService.extractClaim(refreshToken, claims -> claims.get("username", String.class));

                refreshTokenService.deleteRefreshToken(username);
            }

            cookieUtil.deleteRefreshTokenCookie(response);

        } catch (Exception e) {
            System.out.println(e.getMessage());
            cookieUtil.deleteRefreshTokenCookie(response);
        }
    }

    public void logoutFromAllDevices(String username, HttpServletResponse response) {
        try {
            refreshTokenService.deleteRefreshToken(username);
            cookieUtil.deleteRefreshTokenCookie(response);
        } catch (Exception e) {
        }
    }
}