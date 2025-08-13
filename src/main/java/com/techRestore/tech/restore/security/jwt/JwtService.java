package com.techRestore.tech.restore.security.jwt;

import com.techRestore.tech.restore.security.userdetails.ShopPrincipal;
import com.techRestore.tech.restore.security.userdetails.UserPrincipal;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Service
public class JwtService {

    private final String jwtSecretKey;
    private final long accessTokenExpiration = 15 * 60 * 1000; // rob3 sa3a
    private final long refreshTokenExpiration = 7 * 24 * 60 * 60 * 1000; //7 ayam

    public JwtService(@Value("${jwt.secret}") String jwtSecretKey) {
        this.jwtSecretKey = jwtSecretKey;
    }

    public String generateAccessToken(Authentication authentication) {
        return generateToken(authentication, accessTokenExpiration, "access");
    }

    public String generateRefreshToken(Authentication authentication) {
        return generateToken(authentication, refreshTokenExpiration, "refresh");
    }

    private String generateToken(Authentication authentication, long expiration, String tokenType) {
        SecretKey secret = Keys.hmacShaKeyFor(jwtSecretKey.getBytes(StandardCharsets.UTF_8));

        String currentRole = "GUEST";
        Object principal =  authentication.getPrincipal();
        if (principal instanceof UserPrincipal userPrincipal) {
            currentRole = userPrincipal.getAuthorities().iterator().next()
                    .getAuthority().replace("ROLE_", "");
        } else if (principal instanceof ShopPrincipal shopPrincipal) {
            currentRole = shopPrincipal.getAuthorities().iterator().next()
                    .getAuthority().replace("ROLE_", "");
        }

        return Jwts.builder()
                .issuer("Tech Restore")
                .subject("JWT Token")
                .claim("username", authentication.getName())
                .claim("roles", "ROLE_" + currentRole)
                .claim("tokenType", tokenType)
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .issuedAt(new Date())
                .signWith(secret)
                .compact();
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public String extractTokenType(String token) {
        return extractClaim(token, claims -> claims.get("tokenType", String.class));
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public <T> T extractClaim(String token, ClaimsResolver<T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.resolve(claims);
    }

    private Claims extractAllClaims(String token) {
        SecretKey secret = Keys.hmacShaKeyFor(jwtSecretKey.getBytes(StandardCharsets.UTF_8));
        return Jwts.parser()
                .verifyWith(secret)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    public boolean isValidToken(String token, String username) {
        final String tokenUsername = extractClaim(token, claims -> claims.get("username", String.class));
        return (tokenUsername.equals(username) && !isTokenExpired(token));
    }

    public boolean isAccessToken(String token) {
        return "access".equals(extractTokenType(token));
    }

    public boolean isRefreshToken(String token) {
        return "refresh".equals(extractTokenType(token));
    }

    @FunctionalInterface
    public interface ClaimsResolver<T> {
        T resolve(Claims claims);
    }
}
