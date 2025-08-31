package com.techRestore.tech.restore.services;

import com.techRestore.tech.restore.utils.EmailVerificationUtils;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;

public class EmailVerificationUtilsTest {

    @Test
    void generateVerificationToken_ShouldReturnNonNullToken() {
        String token = EmailVerificationUtils.generateVerificationToken();
        assertNotNull(token);
        assertFalse(token.isEmpty());
        assertTrue(token.length() > 10);
    }

    @Test
    void generateVerificationToken_ShouldReturnUniqueTokens() {
        String token1 = EmailVerificationUtils.generateVerificationToken();
        String token2 = EmailVerificationUtils.generateVerificationToken();
        assertNotEquals(token1, token2);
    }

    @Test
    void getTokenExpiry_ShouldReturnFutureTime() {
        LocalDateTime expiry = EmailVerificationUtils.getTokenExpiry();
        assertTrue(expiry.isAfter(LocalDateTime.now()));
    }

    @Test
    void isTokenExpired_ShouldReturnTrueForPastTime() {
        LocalDateTime pastTime = LocalDateTime.now().minusHours(1);
        assertTrue(EmailVerificationUtils.isTokenExpired(pastTime));
    }

    @Test
    void isTokenExpired_ShouldReturnFalseForFutureTime() {
        LocalDateTime futureTime = LocalDateTime.now().plusHours(1);
        assertFalse(EmailVerificationUtils.isTokenExpired(futureTime));
    }

    @Test
    void isTokenExpired_ShouldReturnFalseForNullTime() {
        assertFalse(EmailVerificationUtils.isTokenExpired(null));
    }
}