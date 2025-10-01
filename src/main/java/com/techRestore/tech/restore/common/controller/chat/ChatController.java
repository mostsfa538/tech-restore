package com.techRestore.tech.restore.common.controller.chat;

import com.techRestore.tech.restore.common.dto.chat.*;
import com.techRestore.tech.restore.common.model.entities.Shop;
import com.techRestore.tech.restore.common.model.entities.User;
import com.techRestore.tech.restore.common.security.userdetails.ShopPrincipal;
import com.techRestore.tech.restore.common.security.userdetails.UserPrincipal;
import com.techRestore.tech.restore.common.services.Chat.ChatService;
import com.techRestore.tech.restore.shop.repository.ShopRepository;
import com.techRestore.tech.restore.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Controller
@RestController
@RequestMapping("/api/chats")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;
    private final UserRepository userRepository;
    private final ShopRepository shopRepository;

    @PostMapping("/start")
    public ResponseEntity<ChatResponse> startChat(@RequestBody StartChatRequest request) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();

            AuthInfo authInfo = extractAuthInfo(auth);

            if (!authInfo.getUserType().equals("USER")) {
                return ResponseEntity.badRequest()
                        .body(ChatResponse.error("Only users can start chats with shops"));
            }

            ChatResponse response = chatService.startChat(authInfo.getUserId(), request.getShopId());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ChatResponse.error("Failed to start chat: " + e.getMessage()));
        }
    }

    @GetMapping("/sessions")
    public ResponseEntity<List<ChatSessionDTO>> getActiveSessions() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            AuthInfo authInfo = extractAuthInfo(auth);

            List<ChatSessionDTO> sessions;
            if (authInfo.getUserType().equals("USER")) {
                sessions = chatService.getUserActiveSessions(authInfo.getUserId());
            } else {
                sessions = chatService.getShopActiveSessions(authInfo.getUserId());
            }

            return ResponseEntity.ok(sessions);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(List.of());
        }
    }

    @GetMapping("/{sessionId}/messages")
    public ResponseEntity<List<ChatMessageDTO>> getSessionMessages(@PathVariable UUID sessionId) {
        try {

            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            AuthInfo authInfo = extractAuthInfo(auth);

            if (!chatService.hasAccessToSession(sessionId, authInfo.getUserId(), authInfo.getUserType())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(List.of());
            }

            List<ChatMessageDTO> messages = chatService.getSessionMessages(sessionId);
            return ResponseEntity.ok(messages);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(List.of());
        }
    }

    @PostMapping("/{sessionId}/end")
    public ResponseEntity<ChatResponse> endChat(@PathVariable UUID sessionId) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            AuthInfo authInfo = extractAuthInfo(auth);

            ChatResponse response = chatService.endChat(authInfo.getUserId(), authInfo.getUserType(), sessionId);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ChatResponse.error("Failed to end chat: " + e.getMessage()));
        }
    }

    @MessageMapping("/chat/send")
    public void sendMessage(@Payload SendMessageRequest request, SimpMessageHeaderAccessor headerAccessor) {
        try {
            System.out.println("WebSocket sendMessage - Session attributes: " + headerAccessor.getSessionAttributes());

            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            System.out.println("WebSocket sendMessage - SecurityContext Authentication: "
                    + (auth != null ? auth.getName() : "null"));

            if (auth == null || !auth.isAuthenticated()) {
                System.out.println("WebSocket sendMessage - SecurityContext empty, checking session attributes");

                String userEmail = (String) headerAccessor.getSessionAttributes().get("userEmail");
                String userId = (String) headerAccessor.getSessionAttributes().get("userId");
                String userType = (String) headerAccessor.getSessionAttributes().get("userType");

                if (userEmail != null && userId != null && userType != null) {
                    AuthInfo authInfo = new AuthInfo(UUID.fromString(userId), userType, userEmail);
                    System.out.println("WebSocket sendMessage - Using session AuthInfo: " + authInfo.getUserId() + ", "
                            + authInfo.getUserType());

                    chatService.sendMessage(authInfo.getUserId(), authInfo.getUserType(), request);
                    System.out.println("WebSocket sendMessage - Message sent successfully using session data");
                    return;
                } else {
                    System.err.println(
                            "WebSocket sendMessage - No authentication found in SecurityContext or session attributes");
                    return;
                }
            }

            AuthInfo authInfo = extractAuthInfo(auth);

            chatService.sendMessage(authInfo.getUserId(), authInfo.getUserType(), request);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private AuthInfo extractAuthInfo(Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) {
            throw new RuntimeException("User not authenticated");
        }

        String username = auth.getName();
        Object principal = auth.getPrincipal();

        UUID userId;
        String userType;

        if (principal instanceof UserPrincipal userPrincipal) {
            userId = userPrincipal.getUser().getId();
            userType = "USER";
        } else if (principal instanceof ShopPrincipal shopPrincipal) {
            userId = shopPrincipal.getShop().getId();
            userType = "SHOP";
        } else {
            User user = userRepository.findByEmail(username);
            if (user != null) {
                userId = user.getId();
                userType = "USER";
            } else {
                Shop shop = shopRepository.findByEmail(username).orElse(null);
                if (shop != null) {
                    userId = shop.getId();
                    userType = "SHOP";
                } else {
                    throw new RuntimeException("Unable to identify user from authentication");
                }
            }
        }

        return new AuthInfo(userId, userType, username);
    }

    private static class AuthInfo {
        private final UUID userId;
        private final String userType;
        private final String username;

        public AuthInfo(UUID userId, String userType, String username) {
            this.userId = userId;
            this.userType = userType;
            this.username = username;
        }

        public UUID getUserId() {
            return userId;
        }

        public String getUserType() {
            return userType;
        }

        public String getUsername() {
            return username;
        }
    }
}
