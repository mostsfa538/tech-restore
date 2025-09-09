package com.techRestore.tech.restore.common.security.jwt;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.techRestore.tech.restore.common.model.entities.RefreshToken;
import com.techRestore.tech.restore.common.repository.RefreshTokenRepository;

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

    @Value("${jwt.refresh-token.expiration:604800}") // 7 days in seconds
    private long refreshTokenExpirationSeconds;

    @Transactional
    public void saveRefreshToken(String username, String refreshToken) {
        try {
            refreshTokenRepository.deleteByUsername(username);

            RefreshToken token = new RefreshToken();
            token.setToken(refreshToken);
            token.setUsername(username);
            token.setExpiryDate(LocalDateTime.now().plusSeconds(refreshTokenExpirationSeconds));

            refreshTokenRepository.save(token);
            log.debug("Refresh token saved for user: {}", username);
        } catch (Exception e) {
            log.error("Error saving refresh token for user {}: {}", username, e.getMessage());
            throw new RuntimeException("Failed to save refresh token", e);
        }
    }

    public Optional<RefreshToken> getRefreshToken(String token) {
        try {
            return refreshTokenRepository.findByToken(token);
        } catch (Exception e) {
            log.error("Error retrieving refresh token: {}", e.getMessage());
            return Optional.empty();
        }
    }

    public boolean isValidRefreshToken(String username, String refreshToken) {
        try {
            Optional<RefreshToken> tokenOpt = refreshTokenRepository.findByToken(refreshToken);

            if (tokenOpt.isEmpty()) {
                log.debug("Refresh token not found in database");
                return false;
            }

            RefreshToken token = tokenOpt.get();

            boolean isValid = token.getUsername().equals(username) && !token.isExpired();

            if (!isValid) {
                log.debug("Refresh token validation failed for user: {}", username);
                refreshTokenRepository.delete(token);
            }

            return isValid;
        } catch (Exception e) {
            log.error("Error validating refresh token for user {}: {}", username, e.getMessage());
            return false;
        }
    }

    @Transactional
    public void deleteRefreshToken(String username) {
        try {
            refreshTokenRepository.deleteByUsername(username);
            log.debug("Refresh token deleted for user: {}", username);
        } catch (Exception e) {
            log.error("Error deleting refresh token for user {}: {}", username, e.getMessage());
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

    @Transactional
    public void deleteAllRefreshTokens() {
        try {
            refreshTokenRepository.deleteAll();
            log.info("All refresh tokens deleted");
        } catch (Exception e) {
            log.error("Error deleting all refresh tokens: {}", e.getMessage());
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
}