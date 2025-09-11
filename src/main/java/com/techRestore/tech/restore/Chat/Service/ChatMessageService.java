package com.techRestore.tech.restore.Chat.Service;

import java.security.Principal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import com.techRestore.tech.restore.Chat.Exceptoin.ResourceNotFoundException;
import com.techRestore.tech.restore.Chat.Repo.ChatMessageRepository;
import com.techRestore.tech.restore.Chat.model.ChatMessage;
import com.techRestore.tech.restore.Chat.model.ChatNotification;
import com.techRestore.tech.restore.Chat.model.ChatRoom;
import com.techRestore.tech.restore.Chat.model.MessageStatus;
import com.techRestore.tech.restore.common.model.enums.SenderRole;
import com.techRestore.tech.restore.shop.repository.ShopRepository;
import com.techRestore.tech.restore.user.repository.UserRepository;
import com.techRestore.tech.restore.Chat.model.ConversationSummary;

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

    public void processMessage(ChatMessage chatMessage, Principal principal) {
        try {
            // Validation
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
        } catch (Exception e) {
            System.err.println("Error processing message: " + e.getMessage());
            throw e;
        }
    }

    public ChatMessage save(ChatMessage chatMessage) {
        chatMessage.setStatus(MessageStatus.RECEIVED);
        
        if (chatMessage.getTimestamp() == null) {
            chatMessage.setTimestamp(new Date());
        }
        
        return repository.save(chatMessage);
    }

    public long countNewMessages(UUID senderId, UUID recipientId, Principal principal) {
        try {
            if (!isAuthorizedUser(principal, senderId, recipientId)) {
                throw new SecurityException("Access denied");
            }
            return repository.countBySenderIdAndRecipientIdAndStatus(senderId, recipientId, MessageStatus.RECEIVED);
        } catch (Exception e) {
            System.err.println("Error counting messages: " + e.getMessage());
            throw e;
        }
    }

    public ResponseEntity<?> findChatMessages(UUID senderId, UUID recipientId, Principal principal) {
        try {
            if (!isAuthorizedUser(principal, senderId, recipientId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Access denied: Can only view your own conversations"));
            }

            var chatId = chatRoomService.getChatId(senderId, recipientId, false);
            var messages = chatId.map(repository::findByChatId).orElse(new ArrayList<>());
           
            if (!messages.isEmpty()) {
                UUID currentUserId = getCurrentUserId(principal);
                markMessagesAsDelivered(senderId, recipientId, currentUserId);
            }
           
            return ResponseEntity.ok(messages);
        } catch (Exception e) {
            System.err.println("Error finding chat messages: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to retrieve messages"));
        }
    }

    public ResponseEntity<?> findMessageById(String id, Principal principal) {
        try {
            ChatMessage message = repository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Message not found: " + id));
            
            if (!isMessageOwner(principal, message)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Access denied: Can only view your own messages"));
            }
            
            UUID currentUserId = getCurrentUserId(principal);
            if (currentUserId.equals(message.getRecipientId()) && 
                message.getStatus() == MessageStatus.RECEIVED) {
                message.setStatus(MessageStatus.DELIVERED);
                message = repository.save(message);
            }
            
            return ResponseEntity.ok(message);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Message not found"));
        } catch (Exception e) {
            System.err.println("Error finding message: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to retrieve message"));
        }
    }

    public ResponseEntity<?> getUserConversations(Principal principal) {
        try {
            UUID currentUserId = getCurrentUserId(principal);
            List<ChatRoom> userChatRooms = chatRoomService.getUserChatRooms(currentUserId);
            
            List<ConversationSummary> conversations = userChatRooms.stream()
                    .map(chatRoom -> {
                        UUID otherUserId = chatRoom.getRecipientId();
                        String otherUserName = getDisplayName(otherUserId);
                        SenderRole otherUserRole = chatRoom.getRecipientRole();
                        
                        // Get last message in this chat
                        List<ChatMessage> recentMessages = repository.findRecentMessagesByChatId(
                                chatRoom.getChatId(), 
                                PageRequest.of(0, 1)
                        );
                        
                        String lastMessage = "";
                        Date lastMessageTime = null;
                        if (!recentMessages.isEmpty()) {
                            ChatMessage lastMsg = recentMessages.get(0);
                            lastMessage = lastMsg.getContent();
                            lastMessageTime = lastMsg.getTimestamp();
                        }
                        
                        // Count unread messages
                        long unreadCount = repository.countUnreadMessagesInConversation(
                                currentUserId, otherUserId, MessageStatus.RECEIVED);
                        
                        return ConversationSummary.builder()
                                .otherUserId(otherUserId)
                                .otherUserName(otherUserName)
                                .otherUserRole(otherUserRole)
                                .lastMessage(lastMessage)
                                .lastMessageTimestamp(lastMessageTime)
                                .unreadCount(unreadCount)
                                .chatId(chatRoom.getChatId())
                                .build();
                    })
                    .collect(Collectors.toList());
            
            return ResponseEntity.ok(conversations);
        } catch (Exception e) {
            System.err.println("Error getting user conversations: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to retrieve conversations"));
        }
    }

    public ResponseEntity<?> markConversationAsRead(UUID senderId, UUID recipientId, Principal principal) {
        try {
            if (!isAuthorizedUser(principal, senderId, recipientId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Access denied"));
            }
            
            UUID currentUserId = getCurrentUserId(principal);
            markMessagesAsDelivered(senderId, recipientId, currentUserId);
            
            return ResponseEntity.ok(Map.of("message", "Messages marked as read"));
        } catch (Exception e) {
            System.err.println("Error marking messages as read: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to mark messages as read"));
        }
    }

    public ResponseEntity<?> deleteConversation(UUID otherUserId, Principal principal) {
        try {
            UUID currentUserId = getCurrentUserId(principal);
            
            var chatId = chatRoomService.getChatId(currentUserId, otherUserId, false);
            if (chatId.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Conversation not found"));
            }
            
            // Delete all messages in the chat
            repository.deleteByChatId(chatId.get());
            
            // Delete chat room entries
            chatRoomService.deleteChatRoom(currentUserId, otherUserId);
            
            return ResponseEntity.ok(Map.of("message", "Conversation deleted successfully"));
        } catch (Exception e) {
            System.err.println("Error deleting conversation: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to delete conversation"));
        }
    }

    public ResponseEntity<?> getTotalUnreadCount(Principal principal) {
        try {
            UUID currentUserId = getCurrentUserId(principal);
            long unreadCount = repository.countUnreadMessagesForRecipient(currentUserId, MessageStatus.RECEIVED);
            
            return ResponseEntity.ok(Map.of("unreadCount", unreadCount));
        } catch (Exception e) {
            System.err.println("Error getting unread count: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to get unread count"));
        }
    }

    public ResponseEntity<?> checkChatRoomExists(UUID userId1, UUID userId2, Principal principal) {
        try {
            UUID currentUserId = getCurrentUserId(principal);
            if (!currentUserId.equals(userId1) && !currentUserId.equals(userId2)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Access denied"));
            }
            
            boolean exists = chatRoomService.getChatId(userId1, userId2, false).isPresent();
            return ResponseEntity.ok(Map.of(
                "exists", exists,
                "userId1", userId1,
                "userId2", userId2
            ));
        } catch (Exception e) {
            System.err.println("Error checking chat room: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to check chat room"));
        }
    }

    // Helper method to get display name for user or shop
    private String getDisplayName(UUID userId) {
        var user = userRepository.findById(userId);
        if (user.isPresent()) {
            return user.get().getFirst_name() + " " + user.get().getLast_name();
        }
        
        var shop = shopRepository.findById(userId);
        if (shop.isPresent()) {
            return shop.get().getName();
        }
        
        return "Unknown User";
    }

    // Public method for controller access
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
            throw new RuntimeException("Both sender and recipient must be valid users");
        }

        if (!((senderIsUser && recipientIsShop) || (senderIsShop && recipientIsUser))) {
            throw new RuntimeException("Chat is only allowed between Guest and ShopOwner");
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
        try {
            messagingTemplate.convertAndSendToUser(
                    saved.getRecipientId().toString(),
                    "/queue/messages",
                    new ChatNotification(
                            saved.getId(),
                            saved.getSenderId(),
                            saved.getSenderName()
                    )
            );
        } catch (Exception e) {
            System.err.println("Failed to send notification: " + e.getMessage());
        }
    }

    private boolean isAuthorizedUser(Principal principal, UUID senderId, UUID recipientId) {
        try {
            UUID currentUserId = getCurrentUserId(principal);
            return currentUserId.equals(senderId) || currentUserId.equals(recipientId);
        } catch (Exception e) {
            return false;
        }
    }

    private boolean isMessageOwner(Principal principal, ChatMessage message) {
        try {
            UUID currentUserId = getCurrentUserId(principal);
            return currentUserId.equals(message.getSenderId()) || 
                   currentUserId.equals(message.getRecipientId());
        } catch (Exception e) {
            return false;
        }
    }
}
