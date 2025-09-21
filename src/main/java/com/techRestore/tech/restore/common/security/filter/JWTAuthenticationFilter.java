package com.techRestore.tech.restore.common.security.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.techRestore.tech.restore.common.dto.common.ErrorResponse;
import com.techRestore.tech.restore.common.security.jwt.JwtService;
import com.techRestore.tech.restore.common.security.userdetails.AssignerDetailsServiceImpl;
import com.techRestore.tech.restore.common.security.userdetails.DeliveryDetailsServiceImpl;
import com.techRestore.tech.restore.common.security.userdetails.ShopDetailsServiceImpl;
import com.techRestore.tech.restore.common.security.userdetails.UserDetailsServiceImpl;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JWTAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    private final UserDetailsServiceImpl userDetailsServiceImpl;
    private final ShopDetailsServiceImpl shopDetailsService;
    private final DeliveryDetailsServiceImpl deliveryDetailsService;
    private final AssignerDetailsServiceImpl assignerDetailsService;

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
            if (jwtService.isTokenExpired(jwt)) {
                handleExpiredToken(response);
                return;
            }

            username = jwtService.extractClaim(jwt, claims -> claims.get("username", String.class));

            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                if (jwtService.isAccessToken(jwt) && jwtService.isValidToken(jwt, username)) {
                    
                    List<SimpleGrantedAuthority> authorities = jwtService.extractAuthoritiesFromToken(jwt);
                    
                    UserDetails userDetails = null;
                    
                    if (authorities.stream().anyMatch(auth -> auth.getAuthority().equals("ROLE_ASSIGNER"))) {
                        try {
                            userDetails = assignerDetailsService.loadUserByUsername(username);
                        } catch (UsernameNotFoundException e) {
                        }
                    } else if (authorities.stream().anyMatch(auth -> auth.getAuthority().equals("ROLE_DELIVERY"))) {
                        try {
                            userDetails = deliveryDetailsService.loadUserByUsername(username);
                        } catch (UsernameNotFoundException e) {
                        }
                    } else if (authorities.stream().anyMatch(auth -> 
                        auth.getAuthority().equals("ROLE_SELLER") || 
                        auth.getAuthority().equals("ROLE_REPAIRER") || 
                        auth.getAuthority().equals("ROLE_BOTH"))) {
                        try {
                            userDetails = shopDetailsService.loadUserByUsername(username);
                        } catch (UsernameNotFoundException e) {
                        }
                    } else {
                        try {
                            userDetails = userDetailsServiceImpl.loadUserByUsername(username);
                        } catch (UsernameNotFoundException e) {
                        }
                    }

                    if (userDetails != null) {
                        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                                userDetails,  
                                null,
                                userDetails.getAuthorities());
                        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(authToken);
                    } else {
                        handleInvalidToken(response);
                        return;
                    }
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
        List<String> publicPaths = Arrays.asList(
                "/api/auth/login",
                "/api/auth/register/user",
                "/api/auth/register/shop",
                "/api/auth/register/delivery",
                "/api/auth/register/assigner",
                "/api/auth/verify-email",
                "/api/auth/resend-otp",
                "/api/auth/forgot-password",
                "/api/auth/reset-password",
                "/oauth2/authorization/google",
                "/login/oauth2/code/google");
        return publicPaths.stream().anyMatch(path::startsWith);
    }
}
