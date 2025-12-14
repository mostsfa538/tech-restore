package com.techRestore.tech.restore.common.services.Chat;

import com.techRestore.tech.restore.common.dto.chat.ChatMessageDTO;
import com.techRestore.tech.restore.common.model.entities.*;
import com.techRestore.tech.restore.common.repository.ChatMessageRepository;
import com.techRestore.tech.restore.common.repository.ChatSessionRepository;
import com.techRestore.tech.restore.shop.repository.ShopRepository;
import com.techRestore.tech.restore.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class ChatService {

    private final ChatMessageRepository chatMessageRepository;
    private final ChatSessionRepository chatSessionRepository;
    private final UserRepository userRepository;
    private final ShopRepository shopRepository;

    public ChatSession getOrCreateChatSession(UUID userId, UUID shopId) {
        log.debug("Getting or creating chat session for user: {} and shop: {}", userId, shopId);

        Optional<ChatSession> existingSession = chatSessionRepository.findByUserIdAndShopId(userId, shopId);
        if (existingSession.isPresent()) {
            ChatSession session = existingSession.get();
            if (!session.isActive()) {
                session.setActive(true);
                session.setEndedAt(null);
                return chatSessionRepository.save(session);
            }
            return session;
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
        Shop shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new IllegalArgumentException("Shop not found: " + shopId));

        ChatSession newSession = new ChatSession();
        newSession.setUser(user);
        newSession.setShop(shop);
        newSession.setActive(true);
        newSession.setCreatedAt(LocalDateTime.now());

        return chatSessionRepository.save(newSession);
    }

    public ChatMessageDTO saveChatMessage(UUID userId, UUID shopId, String message, ChatMessage.SenderType senderType) {
        log.debug("Saving chat message from {} to session (user: {}, shop: {})", senderType, userId, shopId);
        log.info("Saving message: userId={}, shopId={}, senderType={}, content={}", userId, shopId, senderType, message);

        ChatSession chatSession = getOrCreateChatSession(userId, shopId);

        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setChatSession(chatSession);
        chatMessage.setSenderId(senderType == ChatMessage.SenderType.USER ? userId : shopId);
        chatMessage.setSenderType(senderType);
        chatMessage.setContent(message);
        chatMessage.setCreatedAt(LocalDateTime.now());

        ChatMessage savedMessage = chatMessageRepository.save(chatMessage);
        return convertToDTO(savedMessage, chatSession);
    }

    public List<ChatMessageDTO> getChatMessages(UUID userId, UUID shopId) {
        log.debug("Fetching chat messages for user: {} and shop: {}", userId, shopId);

        ChatSession chatSession = chatSessionRepository.findByUserIdAndShopId(userId, shopId)
                .orElseThrow(() -> new IllegalArgumentException("Chat session not found"));

        List<ChatMessage> messages = chatMessageRepository.findByChatSessionIdOrderByCreatedAt(chatSession.getId());
        return messages.stream()
                .map(msg -> convertToDTO(msg, chatSession))
                .collect(Collectors.toList());
    }

    public Page<ChatMessageDTO> getChatMessagesPaginated(UUID userId, UUID shopId, Pageable pageable) {
        log.debug("Fetching paginated chat messages for user: {} and shop: {}", userId, shopId);

        ChatSession chatSession = chatSessionRepository.findByUserIdAndShopId(userId, shopId)
                .orElseThrow(() -> new IllegalArgumentException("Chat session not found"));

        Page<ChatMessage> messagesPage = chatMessageRepository.findByChatSessionOrderByCreatedAtDesc(chatSession, pageable);
        return messagesPage.map(msg -> convertToDTO(msg, chatSession));
    }

    public void markMessagesAsRead(UUID userId, UUID shopId, ChatMessage.SenderType readerType) {
        log.debug("Marking messages as read in chat between user: {} and shop: {}", userId, shopId);

        ChatSession chatSession = chatSessionRepository.findByUserIdAndShopId(userId, shopId)
                .orElseThrow(() -> new IllegalArgumentException("Chat session not found"));

        ChatMessage.SenderType messageSenderType = (readerType == ChatMessage.SenderType.USER)
                ? ChatMessage.SenderType.SHOP : ChatMessage.SenderType.USER;

        List<ChatMessage> unreadMessages = chatMessageRepository.findByChatSessionAndSenderTypeAndIsReadFalse(
                chatSession,
                messageSenderType);

        unreadMessages.forEach(msg -> {
            msg.setRead(true);
            msg.setReadAt(LocalDateTime.now());
        });

        chatMessageRepository.saveAll(unreadMessages);
        log.info("Marked {} messages as read", unreadMessages.size());
    }

    public long getUnreadMessageCount(UUID userId) {
        log.debug("Getting unread message count for user: {}", userId);

        return chatSessionRepository.findByUserId(userId).stream()
                .mapToLong(session -> chatMessageRepository.countByChatSessionAndIsReadFalse(session))
                .sum();
    }

    public void closeChatSession(UUID userId, UUID shopId) {
        log.debug("Closing chat session for user: {} and shop: {}", userId, shopId);

        ChatSession chatSession = chatSessionRepository.findByUserIdAndShopId(userId, shopId)
                .orElseThrow(() -> new IllegalArgumentException("Chat session not found"));

        chatSession.endChat();
        chatSessionRepository.save(chatSession);
    }

    private ChatMessageDTO convertToDTO(ChatMessage message, ChatSession session) {
        return ChatMessageDTO.builder()
                .id(message.getId())
                .userId(session.getUser().getId())
                .userName(session.getUser().getFirst_name() + " " + session.getUser().getLast_name())
                .shopId(session.getShop().getId())
                .shopName(session.getShop().getName())
                .message(message.getContent())
                .sentBy(message.getSenderType().toString())
                .createdAt(message.getCreatedAt())
                .isRead(message.isRead())
                .readAt(message.getReadAt())
                .build();
    }
}