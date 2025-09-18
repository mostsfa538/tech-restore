package com.techRestore.tech.restore.common.security.config;

import java.security.Principal;
import java.util.Collections;
import java.util.List;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.stereotype.Component;

import com.techRestore.tech.restore.common.security.jwt.JwtService;
import com.techRestore.tech.restore.shop.repository.ShopRepository;
import com.techRestore.tech.restore.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class WebSocketAuthenticationInterceptor implements ChannelInterceptor {
    
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final ShopRepository shopRepository;
    
    // Constructor to verify interceptor is created
    // public WebSocketAuthenticationInterceptor(JwtService jwtService, UserRepository userRepository, ShopRepository shopRepository) {
    //     this.jwtService = jwtService;
    //     this.userRepository = userRepository;
    //     this.shopRepository = shopRepository;
    //     System.out.println("WebSocketAuthenticationInterceptor created successfully");
    // }
    
    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
        
        System.out.println("=== WebSocket Interceptor Called ===");
        System.out.println("WebSocket interceptor - Command: " + accessor.getCommand());
        System.out.println("WebSocket interceptor - Headers: " + accessor.toNativeHeaderMap());
        System.out.println("WebSocket interceptor - Message Type: " + message.getClass().getSimpleName());
        
        if (StompCommand.CONNECT.equals(accessor.getCommand()) || 
            StompCommand.SEND.equals(accessor.getCommand()) ||
            StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {
            
            String token = accessor.getFirstNativeHeader("Authorization");
            if (token != null && token.startsWith("Bearer ")) {
                try {
                    authenticateUser(token, accessor);
                } catch (Exception e) {
                    System.err.println("WebSocket authentication failed: " + e.getMessage());
                    return null;
                }
            } else {
                if (!StompCommand.CONNECT.equals(accessor.getCommand())) {
                    Authentication currentAuth = SecurityContextHolder.getContext().getAuthentication();
                    if (currentAuth == null || !currentAuth.isAuthenticated()) {
                        Authentication sessionAuth = (Authentication) accessor.getSessionAttributes().get("authentication");
                        if (sessionAuth != null) {
                            SecurityContextHolder.getContext().setAuthentication(sessionAuth);
                            System.out.println("WebSocket " + accessor.getCommand() + " - Restored authentication from session: " + sessionAuth.getName());
                        } else {
                            System.err.println("WebSocket " + accessor.getCommand() + " - User not authenticated and no token provided");
                            return null;
                        }
                    } else {
                        System.out.println("WebSocket " + accessor.getCommand() + " - User already authenticated: " + currentAuth.getName());
                    }
                } else {
                    System.err.println("Missing or invalid Authorization header in WebSocket connection");
                    return null;
                }
            }
        }
        
        
        return message;
    }
    
    private void authenticateUser(String token, StompHeaderAccessor accessor) {
        try {
            String jwtToken = token.substring(7);
            
            if (!jwtService.isTokenExpired(jwtToken) && jwtService.isAccessToken(jwtToken)) {
                String userEmail = jwtService.extractClaim(jwtToken,
                    claims -> claims.get("username", String.class));
                
                if (jwtService.isValidToken(jwtToken, userEmail)) {
                    AuthenticatedUser authenticatedUser = resolveUserDetails(userEmail);
                    
                    if (authenticatedUser == null) {
                        throw new AuthenticationException("User not found: " + userEmail) {
                            private static final long serialVersionUID = 1L;
                        };
                    }
                    
                    List<GrantedAuthority> authorities = Collections.singletonList(
                        new SimpleGrantedAuthority("ROLE_" + authenticatedUser.getRole())
                    );
                    
                    Authentication authentication = new UsernamePasswordAuthenticationToken(
                        userEmail, null, authorities
                    );
                    
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    
                    Principal principal = () -> userEmail;
                    accessor.setUser(principal);
                    
                    accessor.getSessionAttributes().put("userId", authenticatedUser.getUserId());
                    accessor.getSessionAttributes().put("userEmail", userEmail);
                    accessor.getSessionAttributes().put("userRole", authenticatedUser.getRole());
                    accessor.getSessionAttributes().put("userType", authenticatedUser.getUserType());
                    accessor.getSessionAttributes().put("authentication", authentication);
                    
                    System.out.println("WebSocket authentication successful for user: " + userEmail);
                    
                } else {
                    throw new AuthenticationException("Token validation failed") {
                        private static final long serialVersionUID = 1L;
                    };
                }
            } else {
                throw new AuthenticationException("Invalid or expired token") {
                    private static final long serialVersionUID = 1L;
                };
            }
        } catch (Exception e) {
            System.err.println("WebSocket authentication failed: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    private AuthenticatedUser resolveUserDetails(String email) {
        var user = userRepository.findByEmail(email);
        if (user != null) {
            return new AuthenticatedUser(
                user.getId().toString(),
                user.getRole().name(),
                "USER"
            );
        }
        
        var shop = shopRepository.findByEmail(email);
        if (shop.isPresent()) {
            return new AuthenticatedUser(
                shop.get().getId().toString(),
                "SHOP_OWNER",
                "SHOP"
            );
        }
        
        return null;
    }
    

    private static class AuthenticatedUser {
        private final String userId;
        private final String role;
        private final String userType;
        
        public AuthenticatedUser(String userId, String role, String userType) {
            this.userId = userId;
            this.role = role;
            this.userType = userType;
        }
        
        public String getUserId() { return userId; }
        public String getRole() { return role; }
        public String getUserType() { return userType; }
    }
}