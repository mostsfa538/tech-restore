package com.techRestore.tech.restore.common.controller.chat;

import com.techRestore.tech.restore.common.dto.chat.ChatMessageDTO;
import com.techRestore.tech.restore.common.dto.WebSocketMessageDTO;
import com.techRestore.tech.restore.common.model.entities.ChatMessage;
import com.techRestore.tech.restore.common.services.Chat.ChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;

import java.time.LocalDateTime;
import java.util.UUID;

@Controller
@RequiredArgsConstructor
@Slf4j
public class ChatController {

    private final ChatService chatService;
    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/chat/user/{userId}/shop/{shopId}")
    @SendTo("/topic/chat/{userId}/{shopId}")
    public WebSocketMessageDTO handleChatMessage(
            @DestinationVariable UUID userId,
            @DestinationVariable UUID shopId,
            @Payload WebSocketMessageDTO messageDTO,
            SimpMessageHeaderAccessor headerAccessor) {
        log.info("Handling chat message from user: {} to shop: {}", userId, shopId);

        try {
            Authentication sessionAuth = (Authentication) headerAccessor.getSessionAttributes().get("authentication");
            if (sessionAuth != null) {
                SecurityContextHolder.getContext().setAuthentication(sessionAuth);
                log.info("Set authentication from session: {}", sessionAuth.getName());
                log.info("Authorities: {}", sessionAuth.getAuthorities());
            } else {
                throw new SecurityException("No authentication found in session");
            }

            String senderType = determineSenderType();

            String principalName = null;

            if (headerAccessor.getUser() != null) {
                principalName = headerAccessor.getUser().getName();
            } else {
                String userType = (String) headerAccessor.getSessionAttributes().get("userType");
                String userIdFromSession = (String) headerAccessor.getSessionAttributes().get("userId");
                if (userType != null && userIdFromSession != null) {
                    principalName = userType + "-" + userIdFromSession;
                }
            }

            if (principalName == null) {
                throw new SecurityException("Unable to determine principal");
            }

            log.info("Principal name: {}", principalName);

            int firstHyphen = principalName.indexOf("-");
            if (firstHyphen == -1) {
                throw new SecurityException("Invalid principal format");
            }

            String authType = principalName.substring(0, firstHyphen);
            String authIdString = principalName.substring(firstHyphen + 1);
            UUID authId = UUID.fromString(authIdString);

            log.info("Auth type: {}, Auth ID: {}", authType, authId);

            if ("USER".equals(senderType)) {
                if (!authId.equals(userId)) {
                    throw new SecurityException("Unauthorized: Mismatched user ID");
                }
            } else if ("SHOP".equals(senderType)) {
                if (!authId.equals(shopId)) {
                    throw new SecurityException("Unauthorized: Mismatched shop ID");
                }
            }

            log.info("Received payload: {}", messageDTO.getPayload());

            ChatMessageDTO savedMessage = chatService.saveChatMessage(
                    userId,
                    shopId,
                    messageDTO.getPayload() != null ? messageDTO.getPayload().toString() : "",
                    ChatMessage.SenderType.valueOf(senderType));

            return WebSocketMessageDTO.builder()
                    .type("CHAT")
                    .action("SEND")
                    .payload(savedMessage)
                    .senderId(messageDTO.getSenderId())
                    .senderType(senderType)
                    .recipientId(messageDTO.getRecipientId())
                    .status("SUCCESS")
                    .timestamp(LocalDateTime.now())
                    .build();
        } catch (Exception e) {
            log.error("Error handling chat message", e);
            return WebSocketMessageDTO.builder()
                    .type("CHAT")
                    .action("SEND")
                    .status("ERROR")
                    .message(e.getMessage())
                    .timestamp(LocalDateTime.now())
                    .build();
        }
    }

    @MessageMapping("/chat/{userId}/{shopId}/read")
    public void handleMarkAsRead(
            @DestinationVariable UUID userId,
            @DestinationVariable UUID shopId,
            @Payload WebSocketMessageDTO messageDTO,
            SimpMessageHeaderAccessor headerAccessor) {
        log.info("Marking messages as read for user: {} and shop: {}", userId, shopId);

        try {
            Authentication sessionAuth = (Authentication) headerAccessor.getSessionAttributes().get("authentication");
            if (sessionAuth != null) {
                SecurityContextHolder.getContext().setAuthentication(sessionAuth);
            } else {
                throw new SecurityException("No authentication found in session");
            }

            String senderType = determineSenderType();

            String principalName = null;
            if (headerAccessor.getUser() != null) {
                principalName = headerAccessor.getUser().getName();
            } else {
                String userType = (String) headerAccessor.getSessionAttributes().get("userType");
                String userIdFromSession = (String) headerAccessor.getSessionAttributes().get("userId");
                if (userType != null && userIdFromSession != null) {
                    principalName = userType + "-" + userIdFromSession;
                }
            }

            if (principalName != null) {
                int firstHyphen = principalName.indexOf("-");
                if (firstHyphen != -1) {
                    String authType = principalName.substring(0, firstHyphen);
                    String authIdString = principalName.substring(firstHyphen + 1);
                    UUID authId = UUID.fromString(authIdString);

                    if ("USER".equals(senderType)) {
                        if (!authId.equals(userId)) {
                            throw new SecurityException("Unauthorized: Mismatched user ID");
                        }
                    } else if ("SHOP".equals(senderType)) {
                        if (!authId.equals(shopId)) {
                            throw new SecurityException("Unauthorized: Mismatched shop ID");
                        }
                    }
                }
            }

            chatService.markMessagesAsRead(userId, shopId, ChatMessage.SenderType.valueOf(senderType));

            messagingTemplate.convertAndSend(
                    "/topic/chat/" + userId + "/" + shopId,
                    WebSocketMessageDTO.builder()
                            .type("READ_RECEIPT")
                            .action("READ")
                            .senderId(messageDTO.getSenderId())
                            .senderType(senderType)
                            .recipientId(messageDTO.getRecipientId())
                            .status("SUCCESS")
                            .timestamp(LocalDateTime.now())
                            .build());
        } catch (Exception e) {
            log.error("Error marking messages as read", e);
        }
    }

    @MessageMapping("/chat/{userId}/{shopId}/typing")
    public void handleTypingIndicator(
            @DestinationVariable UUID userId,
            @DestinationVariable UUID shopId,
            @Payload WebSocketMessageDTO messageDTO,
            SimpMessageHeaderAccessor headerAccessor) {
        log.debug("Typing indicator from user: {} to shop: {}", userId, shopId);

        try {
            Authentication sessionAuth = (Authentication) headerAccessor.getSessionAttributes().get("authentication");
            if (sessionAuth != null) {
                SecurityContextHolder.getContext().setAuthentication(sessionAuth);
            } else {
                throw new SecurityException("No authentication found in session");
            }

            String senderType = determineSenderType();

            String principalName = null;
            if (headerAccessor.getUser() != null) {
                principalName = headerAccessor.getUser().getName();
            } else {
                String userType = (String) headerAccessor.getSessionAttributes().get("userType");
                String userIdFromSession = (String) headerAccessor.getSessionAttributes().get("userId");
                if (userType != null && userIdFromSession != null) {
                    principalName = userType + "-" + userIdFromSession;
                }
            }

            if (principalName != null) {
                int firstHyphen = principalName.indexOf("-");
                if (firstHyphen != -1) {
                    String authIdString = principalName.substring(firstHyphen + 1);
                    UUID authId = UUID.fromString(authIdString);

                    if ("USER".equals(senderType) && !authId.equals(userId)) {
                        throw new SecurityException("Unauthorized: Mismatched user ID");
                    } else if ("SHOP".equals(senderType) && !authId.equals(shopId)) {
                        throw new SecurityException("Unauthorized: Mismatched shop ID");
                    }
                }
            }

            messagingTemplate.convertAndSend(
                    "/topic/chat/" + userId + "/" + shopId,
                    WebSocketMessageDTO.builder()
                            .type("TYPING")
                            .action("TYPING_START")
                            .senderId(messageDTO.getSenderId())
                            .senderType(senderType)
                            .recipientId(messageDTO.getRecipientId())
                            .timestamp(LocalDateTime.now())
                            .build());
        } catch (Exception e) {
            log.error("Error handling typing indicator", e);
        }
    }

    @MessageMapping("/chat/{userId}/{shopId}/end")
    public void handleEndChat(
            @DestinationVariable UUID userId,
            @DestinationVariable UUID shopId,
            SimpMessageHeaderAccessor headerAccessor) {
        log.info("Ending chat session for user: {} and shop: {}", userId, shopId);

        try {
            // Get authentication from session
            Authentication sessionAuth = (Authentication) headerAccessor.getSessionAttributes().get("authentication");
            if (sessionAuth != null) {
                SecurityContextHolder.getContext().setAuthentication(sessionAuth);
            } else {
                throw new SecurityException("No authentication found in session");
            }

            String senderType = determineSenderType();

            // Get principal and validate
            String principalName = null;
            if (headerAccessor.getUser() != null) {
                principalName = headerAccessor.getUser().getName();
            } else {
                String userType = (String) headerAccessor.getSessionAttributes().get("userType");
                String userIdFromSession = (String) headerAccessor.getSessionAttributes().get("userId");
                if (userType != null && userIdFromSession != null) {
                    principalName = userType + "-" + userIdFromSession;
                }
            }

            if (principalName != null) {
                // Parse principal name correctly
                int firstHyphen = principalName.indexOf("-");
                if (firstHyphen != -1) {
                    String authIdString = principalName.substring(firstHyphen + 1);
                    UUID authId = UUID.fromString(authIdString);

                    if ("USER".equals(senderType) && !authId.equals(userId)) {
                        throw new SecurityException("Unauthorized: Mismatched user ID");
                    } else if ("SHOP".equals(senderType) && !authId.equals(shopId)) {
                        throw new SecurityException("Unauthorized: Mismatched shop ID");
                    }
                }
            }

            chatService.closeChatSession(userId, shopId);

            messagingTemplate.convertAndSend(
                    "/topic/chat/" + userId + "/" + shopId,
                    WebSocketMessageDTO.builder()
                            .type("CHAT")
                            .action("CLOSED")
                            .status("SUCCESS")
                            .message("Chat session closed")
                            .timestamp(LocalDateTime.now())
                            .build());
        } catch (Exception e) {
            log.error("Error ending chat session", e);
        }
    }

    private String determineSenderType() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) {
            log.debug("Determining sender type. Authorities: {}", auth.getAuthorities());

            if (auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_SHOP_OWNER"))) {
                return "SHOP";
            } else if (auth.getAuthorities().stream().anyMatch(a ->  a.getAuthority().equals("ROLE_GUEST"))) {
                return "USER";
            }
        }
        throw new SecurityException("Unable to determine sender type. Authorities: " +
                (auth != null ? auth.getAuthorities() : "No authentication"));
    }
}