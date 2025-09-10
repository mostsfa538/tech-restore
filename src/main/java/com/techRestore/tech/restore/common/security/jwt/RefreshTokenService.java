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
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;

    @Value("${jwt.refresh-token.expiration:604800}") // 7 days in seconds
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
            log.debug("Refresh token saved for user: {}", username);
        } catch (Exception e) {
            log.error("Error saving refresh token for user {}: {}", username, e.getMessage());
            throw new RuntimeException("Failed to save refresh token", e);
        }
    }

    public boolean isValidRefreshToken(String username, String refreshToken, HttpServletRequest request) {
        try {
            Optional<RefreshToken> tokenOpt = refreshTokenRepository.findByToken(refreshToken);

            if (tokenOpt.isEmpty()) {
                log.debug("Refresh token not found in database");
                return false;
            }

            RefreshToken token = tokenOpt.get();
            String currentIpAddress = getClientIpAddress(request);

            boolean isValid = token.getUsername().equals(username) && !token.isExpired();

            if (!isValid) {
                log.debug("Refresh token validation failed for user: {} from IP: {}", username, currentIpAddress);
                refreshTokenRepository.delete(token);
            }

            return isValid;
        } catch (Exception e) {
            log.error("Error validating refresh token for user {}: {}", username, e.getMessage());
            return false;
        }
    }

    @Transactional
    public void deleteAllByUsername(String username) {
        System.out.println("Deleting refresh token for user: " + username);
        try {
            refreshTokenRepository.deleteAllByUsername(username);
        } catch (Exception e) {
            log.error("Error deleting refresh token for user {}: {}", username,
                    e.getMessage());
        }
    }

    @Transactional
    public void deleteRefreshTokenByToken(String token) {
        try {
            Optional<RefreshToken> tokenOpt = refreshTokenRepository.findByToken(token);
            if (tokenOpt.isPresent()) {
                refreshTokenRepository.delete(tokenOpt.get());
                log.debug("Refresh token deleted by token value");
            }
        } catch (Exception e) {
            log.error("Error deleting refresh token by token: {}", e.getMessage());
        }
    }

    @Scheduled(fixedRate = 3600000)
    @Transactional
    public void cleanupExpiredTokens() {
        try {
            refreshTokenRepository.deleteExpiredTokens(LocalDateTime.now());
            log.debug("Expired refresh tokens cleaned up");
        } catch (Exception e) {
            log.error("Error cleaning up expired tokens: {}", e.getMessage());
        }
    }

    public boolean existsByToken(String token) {
        try {
            return refreshTokenRepository.existsByToken(token);
        } catch (Exception e) {
            log.error("Error checking token existence: {}", e.getMessage());
            return false;
        }
    }

    public List<RefreshToken> getActiveTokensForUser(String username) {
        try {
            return refreshTokenRepository.findByUsernameAndExpiryDateAfter(username, LocalDateTime.now());
        } catch (Exception e) {
            log.error("Error retrieving active tokens for user {}: {}", username, e.getMessage());
            return List.of();
        }
    }

    // Security method: Delete tokens from specific IP (useful for suspicious
    // activity)
    @Transactional
    public void deleteTokensByIp(String ipAddress) {
        try {
            refreshTokenRepository.deleteByIp(ipAddress);
            log.info("Deleted all refresh tokens from IP: {}", ipAddress);
        } catch (Exception e) {
            log.error("Error deleting tokens from IP {}: {}", ipAddress, e.getMessage());
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