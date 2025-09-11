package com.techRestore.tech.restore.common.security.config;

import java.security.Principal;
import org.springframework.security.core.AuthenticationException;
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
    
    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
        
        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            String token = accessor.getFirstNativeHeader("Authorization");
            if (token != null && token.startsWith("Bearer ")) {
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
                            
                            // FIXED: Create consistent principal that works for both WebSocket and REST
                            // Using email as principal name for consistency with REST endpoints
                            Principal principal = () -> userEmail;
                            accessor.setUser(principal);
                            
                            // Store all user details in session attributes for easy access
                            accessor.getSessionAttributes().put("userId", authenticatedUser.getUserId());
                            accessor.getSessionAttributes().put("userEmail", userEmail);
                            accessor.getSessionAttributes().put("userRole", authenticatedUser.getRole());
                            accessor.getSessionAttributes().put("userType", authenticatedUser.getUserType());
                            
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
                    return null;
                }
            } else {
                System.err.println("Missing or invalid Authorization header in WebSocket connection");
                return null;
            }
        }
        
        return message;
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