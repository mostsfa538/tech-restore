package com.techRestore.tech.restore.utils;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;

public class EmailVerificationUtils {
    
    private static final SecureRandom random = new SecureRandom();
    
    public static String generateVerificationToken() {
        byte[] token = new byte[32];
        random.nextBytes(token);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(token);
    }
    
    public static LocalDateTime getTokenExpiry() {
        return LocalDateTime.now().plusHours(24); // Token expires in 24 hours
    }
    
    public static boolean isTokenExpired(LocalDateTime expiry) {
        return expiry != null && LocalDateTime.now().isAfter(expiry);
    }
}