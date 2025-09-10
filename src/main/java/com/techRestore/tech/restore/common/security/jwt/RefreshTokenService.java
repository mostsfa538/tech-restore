package com.techRestore.tech.restore.common.security.jwt;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.techRestore.tech.restore.common.model.entities.RefreshToken;
import com.techRestore.tech.restore.common.repository.RefreshTokenRepository;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;

    @Value("${jwt.refresh-token.expiration:604800}")
    private long refreshTokenExpirationSeconds;

    @Transactional
    public void saveRefreshToken(String username, String refreshToken, HttpServletRequest request) {
        try {

            String ipAddress = getClientIpAddress(request);
            String userAgent = getUserAgent(request);


            RefreshToken token = new RefreshToken();
            token.setToken(refreshToken);
            token.setUsername(username);
            token.setIp(ipAddress);
            token.setUserAgent(userAgent);
            token.setExpiryDate(LocalDateTime.now().plusSeconds(refreshTokenExpirationSeconds));

            refreshTokenRepository.save(token);
        } catch (Exception e) {
            throw new RuntimeException("Failed to save refresh token", e);
        }
    }

    public boolean isValidRefreshToken(String username, String refreshToken) {
        try {
            Optional<RefreshToken> tokenOpt = refreshTokenRepository.findByToken(refreshToken);

            if (tokenOpt.isEmpty()) {
                return false;
            }

            RefreshToken token = tokenOpt.get();

            boolean isValid = token.getUsername().equals(username) && !token.isExpired();

            if (!isValid) {
                refreshTokenRepository.delete(token);
            }

            return isValid;
        } catch (Exception e) {
            return false;
        }
    }

    @Transactional
    public void deleteAllByUsername(String username) {
        try {
            refreshTokenRepository.deleteAllByUsername(username);
        } catch (Exception ignored) {
        }
    }

    @Transactional
    public void deleteRefreshTokenByToken(String token) {
        try {
            Optional<RefreshToken> tokenOpt = refreshTokenRepository.findByToken(token);
            tokenOpt.ifPresent(refreshTokenRepository::delete);

        } catch (Exception ignored) {
        }
    }

    @Scheduled(fixedRate = 3600000)
    @Transactional
    public void cleanupExpiredTokens() {
        try {
            refreshTokenRepository.deleteExpiredTokens(LocalDateTime.now());
        } catch (Exception ignored) {
        }
    }

    /**
     * Extract client IP address from request, handling proxy headers
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty() && !"unknown".equalsIgnoreCase(xForwardedFor)) {
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty() && !"unknown".equalsIgnoreCase(xRealIp)) {
            return xRealIp;
        }

        String xForwardedForCloudflare = request.getHeader("CF-Connecting-IP");
        if (xForwardedForCloudflare != null && !xForwardedForCloudflare.isEmpty()) {
            return xForwardedForCloudflare;
        }

        return request.getRemoteAddr();
    }

    /**
     * Extract User-Agent from request
     */
    private String getUserAgent(HttpServletRequest request) {
        String userAgent = request.getHeader("User-Agent");
        return userAgent != null ? userAgent : "Unknown";
    }
}