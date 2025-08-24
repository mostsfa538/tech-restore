package com.techRestore.tech.restore.security.jwt;

import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RefreshTokenService {

    private final Map<String, String> refreshTokenStore = new ConcurrentHashMap<>();

    public void saveRefreshToken(String username, String refreshToken) {
        refreshTokenStore.put(username, refreshToken);
    }

    public String getRefreshToken(String username) {
        return refreshTokenStore.get(username);
    }

    public boolean isValidRefreshToken(String username, String refreshToken) {
        String storedToken = refreshTokenStore.get(username);
        return storedToken != null && storedToken.equals(refreshToken);
    }

    public void deleteRefreshToken(String username) {
        refreshTokenStore.remove(username);
    }

    public void deleteAllRefreshTokens() {
        refreshTokenStore.clear();
    }
}