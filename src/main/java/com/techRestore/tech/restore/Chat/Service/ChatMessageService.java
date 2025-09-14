package com.techRestore.tech.restore.Chat.Service;

import java.security.Principal;
import java.util.*;
import java.util.stream.Collectors;

import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.ConditionalOperators;

import com.techRestore.tech.restore.Chat.Exceptoin.ResourceNotFoundException;
import com.techRestore.tech.restore.Chat.Repo.ChatMessageRepository;
import com.techRestore.tech.restore.Chat.model.ChatMessage;
import com.techRestore.tech.restore.Chat.model.ChatNotification;
import com.techRestore.tech.restore.Chat.model.MessageStatus;
import com.techRestore.tech.restore.Chat.model.ConversationSummary;
import com.techRestore.tech.restore.common.model.enums.SenderRole;
import com.techRestore.tech.restore.shop.repository.ShopRepository;
import com.techRestore.tech.restore.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ChatMessageService {

    private final ChatMessageRepository repository;
    private final MongoOperations mongoOperations;
    private final ChatRoomService chatRoomService;
    private final UserRepository userRepository;
    private final ShopRepository shopRepository;
    private final SimpMessagingTemplate messagingTemplate;

    public ChatMessage processMessage(ChatMessage chatMessage, Principal principal) {
        if (chatMessage == null || chatMessage.getContent() == null || chatMessage.getContent().trim().isEmpty()) {
            throw new IllegalArgumentException("Message content cannot be empty");
        }

        if (chatMessage.getSenderId() == null || chatMessage.getRecipientId() == null) {
            throw new IllegalArgumentException("Sender and recipient IDs are required");
        }

        validateSender(principal, chatMessage.getSenderId());

        UUID senderId = chatMessage.getSenderId();
        UUID recipientId = chatMessage.getRecipientId();

        validateChatParticipants(senderId, recipientId);
        setChatRoles(chatMessage, senderId, recipientId);
        setChatRoom(chatMessage, senderId, recipientId);

        ChatMessage saved = save(chatMessage);
        sendNotification(saved);
        return saved;
    }

    public ChatMessage save(ChatMessage chatMessage) {
        chatMessage.setStatus(MessageStatus.RECEIVED);
        if (chatMessage.getTimestamp() == null) {
            chatMessage.setTimestamp(new Date());
        }
        return repository.save(chatMessage);
    }

    public long countNewMessages(UUID senderId, UUID recipientId, Principal principal) {
        if (!isAuthorizedUser(principal, senderId, recipientId)) {
            throw new SecurityException("Access denied");
        }
        return repository.countBySenderIdAndRecipientIdAndStatus(senderId, recipientId, MessageStatus.RECEIVED);
    }

    public ResponseEntity<?> findChatMessages(UUID senderId, UUID recipientId, Principal principal) {
        if (!isAuthorizedUser(principal, senderId, recipientId)) {
            return ResponseEntity.status(403).body(Map.of("error", "Access denied"));
        }

        var chatId = chatRoomService.getChatId(senderId, recipientId, false);
        var messages = chatId.map(repository::findByChatId).orElse(new ArrayList<>());

        if (!messages.isEmpty()) {
            UUID currentUserId = getCurrentUserId(principal);
            markMessagesAsDelivered(senderId, recipientId, currentUserId);
        }

        return ResponseEntity.ok(messages);
    }

    public ResponseEntity<?> findMessageById(String id, Principal principal) {
        ChatMessage message = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Message not found: " + id));

        if (!isMessageOwner(principal, message)) {
            return ResponseEntity.status(403).body(Map.of("error", "Access denied"));
        }

        UUID currentUserId = getCurrentUserId(principal);
        if (currentUserId.equals(message.getRecipientId()) && message.getStatus() == MessageStatus.RECEIVED) {
            message.setStatus(MessageStatus.DELIVERED);
            message = repository.save(message);
        }

        return ResponseEntity.ok(message);
    }

    public ResponseEntity<?> getUserConversations(Principal principal) {
        UUID currentUserId = getCurrentUserId(principal);

        List<String> chatIds = chatRoomService.getUserChatRooms(currentUserId).stream()
                .map(chatRoom -> chatRoom.getChatId())
                .collect(Collectors.toList());

        if (chatIds.isEmpty()) {
            return ResponseEntity.ok(List.of());
        }

        Aggregation agg = Aggregation.newAggregation(
                Aggregation.match(Criteria.where("chatId").in(chatIds)),
                Aggregation.sort(Sort.by(Sort.Direction.DESC, "timestamp")),
                Aggregation.group("chatId")
                        .last("content").as("lastMessage")
                        .last("timestamp").as("lastMessageTime")
                        .sum(
                                ConditionalOperators.when(
                                        Criteria.where("recipientId").is(currentUserId)
                                                .and("status").is(MessageStatus.RECEIVED)
                                ).then(1).otherwise(0)
                        ).as("unreadCount")
        );

        AggregationResults<ConversationSummary> results =
                mongoOperations.aggregate(agg, "chatMessage", ConversationSummary.class);

        return ResponseEntity.ok(results.getMappedResults());
    }

    public ResponseEntity<?> markConversationAsRead(UUID senderId, UUID recipientId, Principal principal) {
        if (!isAuthorizedUser(principal, senderId, recipientId)) {
            return ResponseEntity.status(403).body(Map.of("error", "Access denied"));
        }

        UUID currentUserId = getCurrentUserId(principal);
        markMessagesAsDelivered(senderId, recipientId, currentUserId);

        return ResponseEntity.ok(Map.of("message", "Messages marked as read"));
    }

    public ResponseEntity<?> deleteConversation(UUID otherUserId, Principal principal) {
        UUID currentUserId = getCurrentUserId(principal);

        var chatId = chatRoomService.getChatId(currentUserId, otherUserId, false);
        if (chatId.isEmpty()) {
            throw new ResourceNotFoundException("Conversation not found");
        }

        repository.deleteByChatId(chatId.get());
        chatRoomService.deleteChatRoom(currentUserId, otherUserId);

        return ResponseEntity.ok(Map.of("message", "Conversation deleted successfully"));
    }

    public ResponseEntity<?> getTotalUnreadCount(Principal principal) {
        UUID currentUserId = getCurrentUserId(principal);
        long unreadCount = repository.countUnreadMessagesForRecipient(currentUserId, MessageStatus.RECEIVED);
        return ResponseEntity.ok(Map.of("unreadCount", unreadCount));
    }

    public ResponseEntity<?> checkChatRoomExists(UUID userId1, UUID userId2, Principal principal) {
        UUID currentUserId = getCurrentUserId(principal);
        if (!currentUserId.equals(userId1) && !currentUserId.equals(userId2)) {
            return ResponseEntity.status(403).body(Map.of("error", "Access denied"));
        }

        boolean exists = chatRoomService.getChatId(userId1, userId2, false).isPresent();
        return ResponseEntity.ok(Map.of(
                "exists", exists,
                "userId1", userId1,
                "userId2", userId2
        ));
    }

    public UUID getCurrentUserId(Principal principal) {
        if (principal == null) {
            throw new SecurityException("Authentication required");
        }

        String email = principal.getName();

        var user = userRepository.findByEmail(email);
        if (user != null) {
            return user.getId();
        }

        var shop = shopRepository.findByEmail(email);
        if (shop.isPresent()) {
            return shop.get().getId();
        }

        throw new SecurityException("User not found: " + email);
    }

    private void markMessagesAsDelivered(UUID senderId, UUID recipientId, UUID currentUserId) {
        Query query = new Query(
                Criteria.where("senderId").is(senderId)
                        .and("recipientId").is(currentUserId)
                        .and("status").is(MessageStatus.RECEIVED));

        Update update = Update.update("status", MessageStatus.DELIVERED);
        mongoOperations.updateMulti(query, update, ChatMessage.class);
    }

    private void validateSender(Principal principal, UUID senderId) {
        if (principal == null) {
            throw new SecurityException("Authentication required");
        }

        UUID currentUserId = getCurrentUserId(principal);
        if (!currentUserId.equals(senderId)) {
            throw new SecurityException("Unauthorized: Cannot send messages as another user");
        }
    }

    private void validateChatParticipants(UUID senderId, UUID recipientId) {
        boolean senderIsUser = userRepository.findById(senderId).isPresent();
        boolean senderIsShop = shopRepository.findById(senderId).isPresent();
        boolean recipientIsUser = userRepository.findById(recipientId).isPresent();
        boolean recipientIsShop = shopRepository.findById(recipientId).isPresent();

        if (!(senderIsUser || senderIsShop) || !(recipientIsUser || recipientIsShop)) {
            throw new IllegalArgumentException("Both sender and recipient must be valid users");
        }

        if (!((senderIsUser && recipientIsShop) || (senderIsShop && recipientIsUser))) {
            throw new IllegalArgumentException("Chat is only allowed between Guest and ShopOwner");
        }
    }

    private void setChatRoles(ChatMessage chatMessage, UUID senderId, UUID recipientId) {
        boolean senderIsUser = userRepository.findById(senderId).isPresent();
        boolean recipientIsUser = userRepository.findById(recipientId).isPresent();

        chatMessage.setSenderRole(senderIsUser ? SenderRole.GUEST : SenderRole.SHOP_OWNER);
        chatMessage.setRecipientRole(recipientIsUser ? SenderRole.GUEST : SenderRole.SHOP_OWNER);
    }

    private void setChatRoom(ChatMessage chatMessage, UUID senderId, UUID recipientId) {
        var chatId = chatRoomService.getChatId(senderId, recipientId, true);
        if (chatId.isEmpty()) {
            throw new RuntimeException("Failed to create or retrieve chat room");
        }
        chatMessage.setChatId(chatId.get());
    }

    private void sendNotification(ChatMessage saved) {
        messagingTemplate.convertAndSendToUser(
                saved.getRecipientId().toString(),
                "/queue/messages",
                new ChatNotification(
                        saved.getId(),
                        saved.getSenderId(),
                        saved.getSenderName()
                )
        );
    }

    private boolean isAuthorizedUser(Principal principal, UUID senderId, UUID recipientId) {
        UUID currentUserId = getCurrentUserId(principal);
        return currentUserId.equals(senderId) || currentUserId.equals(recipientId);
    }

    private boolean isMessageOwner(Principal principal, ChatMessage message) {
        UUID currentUserId = getCurrentUserId(principal);
        return currentUserId.equals(message.getSenderId()) || currentUserId.equals(message.getRecipientId());
    }
}
