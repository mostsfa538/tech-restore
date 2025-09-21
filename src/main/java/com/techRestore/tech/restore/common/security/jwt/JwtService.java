package com.techRestore.tech.restore.common.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import com.techRestore.tech.restore.common.security.userdetails.AssignerPrincipal;
import com.techRestore.tech.restore.common.security.userdetails.DeliveryPrincipal;
import com.techRestore.tech.restore.common.security.userdetails.ShopPrincipal;
import com.techRestore.tech.restore.common.security.userdetails.UserPrincipal;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class JwtService {

    private final String jwtSecretKey;
    private final long accessTokenExpiration = 60 * 60 * 1000; // rob3 sa3a
    private final long refreshTokenExpiration = 7 * 24 * 60 * 60 * 1000; // 7 ayam

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

        Collection<String> roles = new ArrayList<>();
        Object principal = authentication.getPrincipal();
        if (principal instanceof UserPrincipal userPrincipal) {
            roles = userPrincipal.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toList());
        } else if (principal instanceof ShopPrincipal shopPrincipal) {
            roles = shopPrincipal.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toList());
        } else if (principal instanceof DeliveryPrincipal deliveryPrincipal) {
            roles = deliveryPrincipal.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toList());
        } 
        else if (principal instanceof AssignerPrincipal assignerPrincipal) {
            roles = assignerPrincipal.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toList());
        }else {
            roles.add("ROLE_GUEST");
        }

        return Jwts.builder()
                .issuer("Tech Restore")
                .subject("JWT Token")
                .claim("username", authentication.getName())
                .claim("roles", roles)
                .claim("tokenType", tokenType)
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .issuedAt(new Date())
                .signWith(secret)
                .compact();
    }

    public List<SimpleGrantedAuthority> extractAuthoritiesFromToken(String token) {
        return extractClaim(token, claims -> {
            Object rolesObj = claims.get("roles");
            List<SimpleGrantedAuthority> authorities = new ArrayList<>();

            if (rolesObj instanceof List<?> rolesList) {
                // Handle array of roles
                authorities = rolesList.stream()
                        .filter(role -> role instanceof String)
                        .map(role -> new SimpleGrantedAuthority((String) role))
                        .collect(Collectors.toList());
            } else if (rolesObj instanceof String rolesString) {
                String[] roleArray = rolesString.contains(",")
                        ? rolesString.split(",")
                        : new String[]{rolesString};
                authorities = Arrays.stream(roleArray)
                        .map(String::trim)
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toList());
            }

            return authorities;
        });
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
