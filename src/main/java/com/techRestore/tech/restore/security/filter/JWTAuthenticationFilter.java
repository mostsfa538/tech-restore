package com.techRestore.tech.restore.security.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.techRestore.tech.restore.dto.common.ErrorResponse;
import com.techRestore.tech.restore.security.jwt.JwtService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class JWTAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String username;

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        jwt = authHeader.substring(7);

        try {
            // Check if token is expired first
            if (jwtService.isTokenExpired(jwt)) {
                handleExpiredToken(response);
                return;
            }

            username = jwtService.extractClaim(jwt, claims -> claims.get("username", String.class));

            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

                // Verify it's an access token and valid
                if (jwtService.isAccessToken(jwt) && jwtService.isValidToken(jwt, username)) {

                    // Extract roles from token
                    String roles = jwtService.extractClaim(jwt, claims -> claims.get("roles", String.class));
                    List<SimpleGrantedAuthority> authorities = Arrays.stream(roles.split(","))
                            .map(SimpleGrantedAuthority::new)
                            .collect(Collectors.toList());

                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            username,
                            null,
                            authorities);
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                } else {
                    handleInvalidToken(response);
                    return;
                }
            }
        } catch (Exception e) {
            handleInvalidToken(response);
            return;
        }

        filterChain.doFilter(request, response);
    }

    private void handleExpiredToken(HttpServletResponse response) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        ErrorResponse errorResponse = new ErrorResponse(
                "token_expired",
                "Access token has expired",
                "TOKEN_EXPIRED");

        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }

    private void handleInvalidToken(HttpServletResponse response) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        ErrorResponse errorResponse = new ErrorResponse(
                "invalid_token",
                "Invalid access token",
                "INVALID_TOKEN");

        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getServletPath();
        return path.startsWith("/api/auth/");
    }
}
