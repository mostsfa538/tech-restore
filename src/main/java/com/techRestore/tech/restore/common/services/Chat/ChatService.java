package com.techRestore.tech.restore.common.services.Chat;


import com.techRestore.tech.restore.common.dto.chat.*;
import com.techRestore.tech.restore.common.model.entities.*;
import com.techRestore.tech.restore.common.repository.*;
import com.techRestore.tech.restore.shop.repository.ShopRepository;
import com.techRestore.tech.restore.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ChatService {

    private final ChatSessionRepository chatSessionRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final UserRepository userRepository;
    private final ShopRepository shopRepository;
    private final SimpMessagingTemplate messagingTemplate;

    public ChatResponse startChat(UUID userId, UUID shopId) {
        try {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            Shop shop = shopRepository.findById(shopId)
                    .orElseThrow(() -> new RuntimeException("Shop not found"));

            Optional<ChatSession> existingSession = chatSessionRepository
                    .findActiveSessionBetweenUserAndShop(user, shop);

            if (existingSession.isPresent()) {
                ChatSessionDTO sessionDTO = convertToSessionDTO(existingSession.get());
                return ChatResponse.success("Chat session already exists", sessionDTO);
            }

            ChatSession chatSession = new ChatSession();
            chatSession.setUser(user);
            chatSession.setShop(shop);
            chatSession.setActive(true);

            chatSession = chatSessionRepository.save(chatSession);

            ChatSessionDTO sessionDTO = convertToSessionDTO(chatSession);
            messagingTemplate.convertAndSendToUser(
                    shop.getEmail(),
                    "/queue/chat/new-session",
                    sessionDTO
            );

            return ChatResponse.success("Chat started successfully", sessionDTO);

        } catch (Exception e) {
            return ChatResponse.error("Failed to start chat: " + e.getMessage());
        }
    }

    public ChatResponse sendMessage(UUID senderId, String senderType, SendMessageRequest request) {
        try {
            ChatSession chatSession = chatSessionRepository.findById(request.getSessionId())
                    .orElseThrow(() -> new RuntimeException("Chat session not found"));

            if (!chatSession.isActive()) {
                return ChatResponse.error("Chat session is not active");
            }

            boolean isValidSender = (senderType.equals("USER") && chatSession.getUser().getId().equals(senderId)) ||
                                  (senderType.equals("SHOP") && chatSession.getShop().getId().equals(senderId));

            if (!isValidSender) {
                return ChatResponse.error("Unauthorized to send message in this chat");
            }

            ChatMessage message = new ChatMessage();
            message.setChatSession(chatSession);
            message.setSenderId(senderId);
            message.setSenderType(ChatMessage.SenderType.valueOf(senderType));
            message.setContent(request.getContent());

            message = chatMessageRepository.save(message);

            ChatMessageDTO messageDTO = convertToMessageDTO(message);

            messagingTemplate.convertAndSendToUser(
                    chatSession.getUser().getEmail(),
                    "/queue/chat/messages/" + chatSession.getId(),
                    messageDTO
            );

            messagingTemplate.convertAndSendToUser(
                    chatSession.getShop().getEmail(),
                    "/queue/chat/messages/" + chatSession.getId(),
                    messageDTO
            );

            return ChatResponse.success("Message sent successfully", messageDTO);

        } catch (Exception e) {
            return ChatResponse.error("Failed to send message: " + e.getMessage());
        }
    }

    public ChatResponse endChat(UUID userId, String userType, UUID sessionId) {
        try {
            ChatSession chatSession = chatSessionRepository.findById(sessionId)
                    .orElseThrow(() -> new RuntimeException("Chat session not found"));

            boolean canEnd = (userType.equals("USER") && chatSession.getUser().getId().equals(userId)) ||
                           (userType.equals("SHOP") && chatSession.getShop().getId().equals(userId));

            if (!canEnd) {
                return ChatResponse.error("Unauthorized to end this chat");
            }

            chatSession.endChat();
            chatSessionRepository.save(chatSession);

            messagingTemplate.convertAndSendToUser(
                    chatSession.getUser().getEmail(),
                    "/queue/chat/ended/" + sessionId,
                    "Chat ended"
            );

            messagingTemplate.convertAndSendToUser(
                    chatSession.getShop().getEmail(),
                    "/queue/chat/ended/" + sessionId,
                    "Chat ended"
            );

            return ChatResponse.success("Chat ended successfully", null);

        } catch (Exception e) {
            return ChatResponse.error("Failed to end chat: " + e.getMessage());
        }
    }

    public List<ChatSessionDTO> getUserActiveSessions(UUID userId) {
        List<ChatSession> sessions = chatSessionRepository.findActiveSessionsByUserId(userId);
        return sessions.stream()
                .map(this::convertToSessionDTO)
                .collect(Collectors.toList());
    }

    public List<ChatSessionDTO> getShopActiveSessions(UUID shopId) {
        List<ChatSession> sessions = chatSessionRepository.findActiveSessionsByShopId(shopId);
        return sessions.stream()
                .map(this::convertToSessionDTO)
                .collect(Collectors.toList());
    }

    public List<ChatMessageDTO> getSessionMessages(UUID sessionId) {
        List<ChatMessage> messages = chatMessageRepository.findByChatSessionIdOrderByCreatedAt(sessionId);
        return messages.stream()
                .map(this::convertToMessageDTO)
                .collect(Collectors.toList());
    }

    private ChatSessionDTO convertToSessionDTO(ChatSession session) {
        ChatSessionDTO dto = new ChatSessionDTO();
        dto.setId(session.getId());
        dto.setUserId(session.getUser().getId());
        dto.setUserName(session.getUser().getDisplayName());
        dto.setShopId(session.getShop().getId());
        dto.setShopName(session.getShop().getName());
        dto.setActive(session.isActive());
        dto.setCreatedAt(session.getCreatedAt());

        // Get last message if exists
        if (!session.getMessages().isEmpty()) {
            ChatMessage lastMessage = session.getMessages().get(session.getMessages().size() - 1);
            dto.setLastMessage(convertToMessageDTO(lastMessage));
        }

        return dto;
    }

    public boolean hasAccessToSession(UUID sessionId, UUID userId, String userType) {
        try {
            ChatSession session = chatSessionRepository.findById(sessionId)
                    .orElse(null);
            
            if (session == null) {
                return false;
            }

            if (userType.equals("USER")) {
                return session.getUser().getId().equals(userId);
            } else if (userType.equals("SHOP")) {
                return session.getShop().getId().equals(userId);
            }

            return false;
        } catch (Exception e) {
            return false;
        }
    }

    private ChatMessageDTO convertToMessageDTO(ChatMessage message) {
        ChatMessageDTO dto = new ChatMessageDTO();
        dto.setId(message.getId());
        dto.setSessionId(message.getChatSession().getId());
        dto.setSenderId(message.getSenderId());
        dto.setSenderType(message.getSenderType().name());
        dto.setContent(message.getContent());
        dto.setCreatedAt(message.getCreatedAt());

        if (message.getSenderType() == ChatMessage.SenderType.USER) {
            dto.setSenderName(message.getChatSession().getUser().getDisplayName());
        } else {
            dto.setSenderName(message.getChatSession().getShop().getName());
        }

        return dto;
    }
}