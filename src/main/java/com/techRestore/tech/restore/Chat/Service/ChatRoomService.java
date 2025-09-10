package com.techRestore.tech.restore.Chat.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;

import com.techRestore.tech.restore.Chat.Repo.ChatRoomRepository;
import com.techRestore.tech.restore.Chat.model.ChatRoom;
import com.techRestore.tech.restore.common.model.enums.SenderRole;
import com.techRestore.tech.restore.shop.repository.ShopRepository;
import com.techRestore.tech.restore.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ChatRoomService {
    
    private final ChatRoomRepository chatRoomRepository;
    private final UserRepository userRepository;
    private final ShopRepository shopRepository;

    public Optional<String> getChatId(UUID senderId, UUID recipientId, boolean createIfNotExist) {
        // FIXED: Search bidirectionally for existing chat rooms
        return findExistingChatRoom(senderId, recipientId)
                .map(ChatRoom::getChatId)
                .or(() -> {
                    if (!createIfNotExist) {
                        return Optional.empty();
                    }
                    
                    return createChatRoom(senderId, recipientId);
                });
    }
    
    /**
     * FIXED: Search for chat room in both directions
     */
    private Optional<ChatRoom> findExistingChatRoom(UUID senderId, UUID recipientId) {
        // First try sender->recipient direction
        var chatRoom = chatRoomRepository.findBySenderIdAndRecipientId(senderId, recipientId);
        if (chatRoom.isPresent()) {
            return chatRoom;
        }
        
        // Then try recipient->sender direction
        return chatRoomRepository.findBySenderIdAndRecipientId(recipientId, senderId);
    }
    
    /**
     * FIXED: Create chat room with proper role assignment
     */
    private Optional<String> createChatRoom(UUID senderId, UUID recipientId) {
        try {
            var chatId = createConsistentChatId(senderId, recipientId);
            
            // Determine roles for sender and recipient
            SenderRole senderRole = determineRole(senderId);
            SenderRole recipientRole = determineRole(recipientId);
            
            // Validate that it's a valid user-shop conversation
            if (!isValidChatParticipants(senderRole, recipientRole)) {
                throw new IllegalArgumentException("Chat is only allowed between Guest and Shop Owner");
            }
            
            // FIXED: Create chat room entries with proper role assignment
            ChatRoom senderRecipient = ChatRoom.builder()
                    .chatId(chatId)
                    .senderId(senderId)
                    .recipientId(recipientId)
                    .senderRole(senderRole)
                    .recipientRole(recipientRole)
                    .build();
                    
            ChatRoom recipientSender = ChatRoom.builder()
                    .chatId(chatId)
                    .senderId(recipientId)
                    .recipientId(senderId)
                    .senderRole(recipientRole)
                    .recipientRole(senderRole)
                    .build();
            
            chatRoomRepository.save(senderRecipient);
            chatRoomRepository.save(recipientSender);
            
            return Optional.of(chatId);
            
        } catch (Exception e) {
            System.err.println("Failed to create chat room: " + e.getMessage());
            return Optional.empty();
        }
    }
    
    /**
     * Determine the role of a user based on their ID
     */
    private SenderRole determineRole(UUID userId) {
        // Check if it's a regular user
        if (userRepository.findById(userId).isPresent()) {
            return SenderRole.GUEST;
        }
        
        // Check if it's a shop
        if (shopRepository.findById(userId).isPresent()) {
            return SenderRole.SHOP_OWNER;
        }
        
        throw new IllegalArgumentException("User not found: " + userId);
    }
    
    /**
     * Validate that the chat is between a Guest and Shop Owner
     */
    private boolean isValidChatParticipants(SenderRole senderRole, SenderRole recipientRole) {
        return (senderRole == SenderRole.GUEST && recipientRole == SenderRole.SHOP_OWNER) ||
               (senderRole == SenderRole.SHOP_OWNER && recipientRole == SenderRole.GUEST);
    }

    private String createConsistentChatId(UUID user1, UUID user2) {
        if (user1.compareTo(user2) < 0) {
            return String.format("%s_%s", user1, user2);
        } else {
            return String.format("%s_%s", user2, user1);
        }
    }
    
    /**
     * Get all chat rooms for a specific user
     */
    public List<ChatRoom> getUserChatRooms(UUID userId) {
        return chatRoomRepository.findBySenderId(userId);
    }
    
    /**
     * Delete a chat room (both directions)
     */
    public void deleteChatRoom(UUID senderId, UUID recipientId) {
        chatRoomRepository.deleteBySenderIdAndRecipientId(senderId, recipientId);
        chatRoomRepository.deleteBySenderIdAndRecipientId(recipientId, senderId);
    }
}
